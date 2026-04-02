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

#tiền xử lý ban đầu
def TienXuLyBanDau(image): # chủ yếu để khoanh vùng và chấm điểm

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # tuongphan = cv2.convertScaleAbs(gray, alpha=1.6, beta=-80)
    blur = cv2.GaussianBlur(gray, (5, 5), 0)

    thresh = cv2.adaptiveThreshold(
        blur,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV,
        101,
        20,
    )
    return thresh


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

# chuẩn hóa 6 mốc nhỏ theo thứ tự: TL, TR, ML, MR, BL, BR
def chuanHoa_6_MocNho(pts):
    """
    Chuẩn hóa 6 mốc nhỏ theo thứ tự:
    0: top-left, 1: top-right,
    2: mid-left, 3: mid-right,
    4: bottom-left, 5: bottom-right
    """
    pts = np.array(pts, dtype="float32")

    if len(pts) != 6:
        print("KHONG DUNG 6 MOC NHO, hien co:", len(pts))
        return None

    # Ảnh đã phối cảnh nên có thể tách 3 hàng bằng cách sort theo y
    pts_sorted_y = pts[np.argsort(pts[:, 1])]
    row_top = pts_sorted_y[0:2]
    row_mid = pts_sorted_y[2:4]
    row_bot = pts_sorted_y[4:6]

    # Mỗi hàng: trái trước, phải sau
    row_top = row_top[np.argsort(row_top[:, 0])]
    row_mid = row_mid[np.argsort(row_mid[:, 0])]
    row_bot = row_bot[np.argsort(row_bot[:, 0])]

    rect6 = np.vstack([row_top, row_mid, row_bot]).astype("float32")
    return rect6

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

    print("Toa do cac moc dinh vi tim duoc:")
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

# hàm khoanh vùng SBD dựa trên 6 mốc nhỏ đã chuẩn hóa, trả về ảnh vùng SBD đã cắt
def KhoanhVungSBD(cacMocNho, warped):

    #cacMocNho phai được chuẩn hóa bang ham chuanHoa_6_MocNho()
    #warped là ảnh đã được phối cảnh 4 mốc lớn, có kích thước chuẩn để tính toán chính xác vị trí SBD

    top_left, top_right, mid_left, mid_right, bot_left, bot_right = cacMocNho

    # Tính khoảng cách giữa các mốc để xác định kích thước và vị trí vùng SBD
    col_gap = top_right[0] - top_left[0]
    row_gap = mid_left[1] - top_left[1]

    #
    sbd_x1 = int(top_left[0] - 1.045 * col_gap)
    sbd_y1 = int(top_left[1] + 0.05 * row_gap)

    sbd_x2 = int(top_left[0] - 0.065 * col_gap)
    sbd_y2 = int(mid_left[1] - 0.048 * row_gap)

    h, w = warped.shape[:2]
    sbd_x1 = max(0, min(w - 1, sbd_x1)) #trên trái
    sbd_y1 = max(0, min(h - 1, sbd_y1))

    sbd_x2 = max(0, min(w - 1, sbd_x2)) # dưới phải
    sbd_y2 = max(0, min(h - 1, sbd_y2))

    #vẽ hình chữ nhật vùng SBD lên ảnh để debug
    cv2.rectangle(warped, (sbd_x1, sbd_y1), (sbd_x2, sbd_y2), (0, 255, 0), 2)

    return warped[sbd_y1:sbd_y2, sbd_x1:sbd_x2] #cắt vùng SBD từ ảnh đã phối cảnh
    #Nguyên tắc:image[y1:y2, x1:x2] vì ảnh được lưu dưới dạng mảng numpy với thứ tự hàng (y) trước cột (x)

#hàm tính điểm số của một cell SBD bằng cách đếm số pixel trắng (255) trong cell đó, trả về điểm số
def tinh_score_o(cell): 
    score = cv2.countNonZero(cell)
    return score

def XuLySOBAODANH(sbd_roi, warped):
    thresholded_sbd_roi = TienXuLyBanDau(sbd_roi) #đưa vùng SBD về ảnh nhị phân 
    #resize vùng SBD về chiều cao 1000 
    resized_sbd_roi = resize_keep_ratio(thresholded_sbd_roi, height=1000)

    cv2.imshow("vung so bao danh nhi phan", resize_keep_ratio(resized_sbd_roi, height=750))
    cv2.waitKey(0)  

    chia_6_cot = resized_sbd_roi.shape[1] // 6 #chia làm 6 
    # cắt từng cột SBD để xử lý riêng
    ds_cot = []
    for j in range(6):
        cot_j = resized_sbd_roi[0:resized_sbd_roi.shape[0], chia_6_cot * j:chia_6_cot * (j + 1)] #cắt 
        ds_cot.append(cot_j) #thêm vào ds
        # cv2.imshow(f"cot{j+1}",(resize_keep_ratio(cot_j, height=750)) )
        # cv2.waitKey(0)

    #cắt mỗi và XỬ LÝ mỗi hàng 
    chia_10_hang = resized_sbd_roi.shape[0] // 10
    SBD = [] # lưu SBD thực
    for j, cot in enumerate(ds_cot): #dung enumerate để có cả chỉ số cột và dữ liệu cột (j:index, cot: ảnh cột)
        score_theoHang = []

        for i in range(10):
            hang_i = cot[chia_10_hang * i : chia_10_hang * (i+1),  0:cot.shape[1]] #cắt hàng i của cột hiện tại

            score = tinh_score_o(hang_i) #tính điểm số của hang i hiện tại
            # print(f"cot {j+1} - hang {i+1}: score = {score}")
            score_theoHang.append(score) #thêm điểm vào mảng lưu tạm

            # cv2.imshow(f"cot {j+1} - hang{i}", resize_keep_ratio(hang_i, height=750))
            # cv2.waitKey(0)
            # cv2.destroyAllWindows()

        #XỬ LÝ TIẾP CÁC TRƯỜNG HỢP: KO TÔ, TÔ 2 Ô. ############
        score_theoHang_copy = sorted(score_theoHang, reverse=True) #sắp xếp điểm số theo thứ tự giảm dần để tìm max dễ hơn
        max1 = score_theoHang_copy[0] #lấy phần tử đầu tiên
        max2 = score_theoHang_copy[1] #lấy phần tử thứ hai

        if max1 == 0:
            print(f"COT {j+1}: KHONG CO O NAO DUOC TO")
            SBD.append("X") #thêm "X" vào SBD thực nếu không có ô nào được tô
        else:
            if max1 - max2 < max1 - 1.25 * max2:
                print(f"COT {j+1}: CO 2 O DUOC TO, CAN XET TIEP")
                SBD.append("X") #thêm "X" vào SBD thực nếu có 2 ô được tô vì không thể xác định chắc chắn ô nào được tô hơn
            else:
                sbd = score_theoHang.index(max1)  #lấy index của điểm max coi như đã tô
                SBD.append(sbd) #thêm index vào mảng SBD thực
        ##############

        # sbd = score_theoHang.index(max(score_theoHang)) # lấy index của điểm max coi như đã tô
       
    print("SBD:", SBD)
    sbd_str = "".join(str(num) for num in SBD)
    print("SBD (chuoi):", sbd_str)

    cv2.putText(warped, f"SBD: {sbd_str}", (10, warped.shape[0] - 20), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 0), 2)
    return sbd_str, warped


def XuLyAnh(img_path):
    img = cv2.imread(img_path) #đọc ảnh 
    if img is None:
        raise ValueError(f"Khong doc duoc anh: {img_path}")

    resized = resize_keep_ratio(img, height=1280) #resize

    moc = Tim_4_Moc_Dinh_Vi2(resized) #tìm vị trí 4 mốc lớn
    if moc is None:
        raise ValueError("Khong tim du 4 moc")

    warped = PhoiCanh(moc, resized) #cắt vào 4 mốc lớn

    #tìm 6 mốc nhỏ
    cacMocNho = Tim_4_Moc_Dinh_Vi2( warped, gioihan_duoi=200, gioihan_tren=700, tra_tam=True) 

    cacMocNho = chuanHoa_6_MocNho(cacMocNho) # sắp xếp 6 mốc nhỏ: TL, TR, ML, MR, BL, BR
    if cacMocNho is None:
        raise ValueError("Khong tim dung 6 moc nho de khoanh vung SBD")

    #vẽ các mốc nhỏ lên ảnh đã phối cảnh để debug
    i = 0 
    for (cx, cy) in cacMocNho:
        cv2.circle(warped, (int(cx), int(cy)), 10, (0, 255, 255), -1)
        cv2.putText(warped, str(i), (int(cx) + 10, int(cy) - 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (255,0, 0), 2)
        i += 1

    #khoanh vùng SBD
    sbd_roi = KhoanhVungSBD(cacMocNho, warped) #phải được chuẩn hóa 6 mốc nhỏ trước

    sbd_str, warped = XuLySOBAODANH(sbd_roi, warped) 


    cv2.imshow("anh da xu ly", resize_keep_ratio(warped, height=750))
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    # for i, (cx, cy) in enumerate(cacMocNho):
    #     print("toa do moc nho:", i, ":", int(cx), int(cy))

    return warped



xuly = XuLyAnh("DO_AN1/test1.jpg")


# sbd_roi = warped[sbd_y1:sbd_y2, sbd_x1:sbd_x2]

# cv2.imshow("da xu ly", resize_keep_ratio(xuly, height=750))
# cv2.waitKey(0)
# cv2.destroyAllWindows()


# mang = [0, 0, 3506, 0, 0]

# mangCopy = sorted(mang, reverse=True) #sắp xếp mảng theo thứ tự giảm dần để tìm max dễ hơn
# max1 = mangCopy[0] #lấy phần tử đầu tiên sau khi sắp xếp là max
# max2 = mangCopy[1] #lấy phần tử thứ hai sau khi sắp xếp là max thứ hai
# if max1 == 0:
#     print("Khong co o nao duoc to")
#     #output = "Khong co o nao duoc to"

# if max1 - max2 < max1 - 1.25 * max2:
#     print("Co 2 o duoc to, can xet tiep")
#     #output = "Co 2 o duoc to, can xet tiep"

# print(mangCopy)
