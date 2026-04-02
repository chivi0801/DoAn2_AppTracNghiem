import cv2
import numpy as np

DEBUG_WINDOWS = False

#resize ảnh
def resize_keep_ratio(image, width=None, height=None):
    (h, w) = image.shape[:2]

    if width is None and height is None:
        return image

    if width is not None:
        ratio = width / float(w)
        dim = (width, int(h * ratio))
    else:
        ratio = height / float(h)
        dim = (int(w * ratio), height)

    return cv2.resize(image, dim, interpolation=cv2.INTER_AREA)


#hiển thị ảnh debug nếu DEBUG_WINDOWS = True
def _show_debug_image(title, image):
    if not DEBUG_WINDOWS:
        return
    cv2.imshow(title, image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()


# sắp xếp 4 điểm: TL, TR, BR, BL
def chuanHoa_LeA4(pts):

    rect = np.zeros((4, 2), dtype="float32") #tạo mảng 4x2 để lưu tọa độ 4 điểm đã sắp xếp

    s = pts.sum(axis=1) #tính tổng x+y cho mỗi điểm để xác định TL và BR
    diff = np.diff(pts, axis=1) #tính hiệu x-y cho mỗi điểm để xác định TR và BL

    rect[0] = pts[np.argmin(s)] # TL có tổng nhỏ nhất
    rect[2] = pts[np.argmax(s)] # BR có tổng lớn nhất
    rect[1] = pts[np.argmin(diff)] # TR có hiệu nhỏ nhâts
    rect[3] = pts[np.argmax(diff)] # BL có hiệu lớn nhất

    return rect

# Tìm 4 mốc định vị trên ảnh, trả về tọa độ của chúng hoặc đã chuẩn hóa nếu tra_tam=False
def Tim_4_Moc_Dinh_Vi2(anh, gioihan_duoi=200, gioihan_tren=900, tra_tam=False):
    
    gray = cv2.cvtColor(anh, cv2.COLOR_BGR2GRAY)
    tuongphan = cv2.convertScaleAbs(gray, alpha=1.6, beta=-80)
    blur = cv2.GaussianBlur(tuongphan, (5, 5), 0)

    thresh = cv2.adaptiveThreshold(
        blur,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV,
        31,
        10,
    )

    # tạm tắt
    # _show_debug_image("adaptive threshold", resize_keep_ratio(thresh.copy(), height=750))

    cnts = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]

    centers = []

    for contour in cnts:
        area = cv2.contourArea(contour) #tính diện tích contour

        if not (gioihan_duoi < area < gioihan_tren):
            continue
        
        
        hull = cv2.convexHull(contour) 
        hull_area = cv2.contourArea(hull)
        if hull_area == 0:
            continue

        solidity = float(area) / hull_area
        if solidity < 0.8:
            continue

        peri = cv2.arcLength(contour, True) #tính chu vi contour
        approx = cv2.approxPolyDP(contour, 0.02 * peri, True) #xấp xỉ đa giác 

        if not (4 <= len(approx) <= 5): #chỉ chấp nhận contour có 4 hoặc 5 điểm xấp xỉ
            continue

        moments = cv2.moments(approx) # tính moment của contour để tìm tâm
        if moments["m00"] == 0:
            continue

        cx = int(moments["m10"] / moments["m00"]) #tính tọa độ x của tâm contour
        cy = int(moments["m01"] / moments["m00"]) #tính tọa độ y của tâm contour

        centers.append((cx, cy))

    print("Tọa độ các mốc định vị tìm được:")
    print(centers)

    output = anh.copy() #vẽ các mốc định vị lên ảnh để debug
    for (cx, cy) in centers:
        cv2.circle(output, (cx, cy), 5, (0, 255, 255), -1)

    # tạm tắt
    # _show_debug_image("cac moc dinh vi", resize_keep_ratio(output, height=750))

    if tra_tam: 
        return centers

    pts_4 = np.array(centers, dtype="float32") #chuyển đổi danh sách tọa độ thành mảng numpy kiểu float32 để chuẩn hóa

    if len(pts_4) < 4:
        print("KHONG DU 4 MOC, chi co:", len(pts_4))
        return None

    return chuanHoa_LeA4(pts_4)


def PhoiCanh(pst, original_img):
    (tl, tr, br, bl) = pst

    widthA = np.linalg.norm(br - bl)
    widthB = np.linalg.norm(tr - tl)
    maxWidth = int(max(widthA, widthB))

    heightA = np.linalg.norm(tr - br)
    heightB = np.linalg.norm(tl - bl)
    maxHeight = int(max(heightA, heightB))

    dst = np.array(
        [
            [-8, -8],
            [maxWidth + 8, -8],
            [maxWidth + 8, maxHeight + 8],
            [-8, maxHeight + 8],
        ],
        dtype="float32",
    )

    matrix = cv2.getPerspectiveTransform(pst, dst)
    warped = cv2.warpPerspective(original_img, matrix, (maxWidth, maxHeight))

    # tạm tắt
    # _show_debug_image("anh da phoi canh", resize_keep_ratio(warped, height=750))

    return warped


def XuLyAnh(img_path):
    img = cv2.imread(img_path)

    if img is None:
        raise ValueError(f"Khong doc duoc anh: {img_path}")

    resized = resize_keep_ratio(img, height=1280)

    moc = Tim_4_Moc_Dinh_Vi2(resized)
    if moc is None:
        raise ValueError("Khong tim du 4 moc")

    warped = PhoiCanh(moc, resized)

    cacMocNho = Tim_4_Moc_Dinh_Vi2( warped, gioihan_duoi=200, gioihan_tren=700, tra_tam=True) #

    for (cx, cy) in cacMocNho:
        cv2.circle(warped, (int(cx), int(cy)), 10, (0, 255, 255), -1)

    # khoanh vùng SBD dựa trên tỉ lệ của ảnh
    h, w = warped.shape[:2]

    sbd_x1 = int(0.1 * w)
    sbd_y1 = int(0.27 * h)
    sbd_x2 = int(0.36 * w)
    sbd_y2 = int(0.6 * h)

    cv2.rectangle(warped, (sbd_x1, sbd_y1), (sbd_x2, sbd_y2), (0, 255, 0), 2)

    # tạm tắt
    # _show_debug_image("SBD REGION", resize_keep_ratio(warped, height=750))

    return warped 