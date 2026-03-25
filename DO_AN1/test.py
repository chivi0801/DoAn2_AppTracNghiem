import cv2
import Tinh_Gon_Code as TGC
import imutils
import numpy as np

DEBUG_WINDOWS = False


def _show_debug_image(title, image):
    if not DEBUG_WINDOWS:
        return
    cv2.imshow(title, image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()


def chuanHoa_LeA4(pts):
    rect = np.zeros((4, 2), dtype="float32")
    s = pts.sum(axis=1)
    diff = np.diff(pts, axis=1)
    rect[0] = pts[np.argmin(s)]
    rect[2] = pts[np.argmax(s)]
    rect[1] = pts[np.argmin(diff)]
    rect[3] = pts[np.argmax(diff)]
    return rect


def Tim_4_Moc_Dinh_Vi2(anhDaPhoiVienA4, gioihan_duoi=200, gioihan_tren=900):
    gray = cv2.cvtColor(anhDaPhoiVienA4, cv2.COLOR_BGR2GRAY)
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

    preview = imutils.resize(thresh.copy(), height=750)
    _show_debug_image("adaptive threshold", preview)

    cnts = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)

    markers = []
    for c in cnts:
        area = cv2.contourArea(c)
        if not (gioihan_duoi < area < gioihan_tren):
            continue

        hull = cv2.convexHull(c)
        hull_area = cv2.contourArea(hull)
        if hull_area == 0:
            continue

        solidity = float(area) / hull_area
        if solidity < 0.8:
            continue

        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)
        if 4 <= len(approx) <= 5:
            markers.append(approx)

    centers = []
    for m in markers:
        moments = cv2.moments(m)
        if moments["m00"] != 0:
            cx = int(moments["m10"] / moments["m00"])
            cy = int(moments["m01"] / moments["m00"])
            centers.append((cx, cy))

    print("Toa do cac moc dinh vi tim duoc:")
    print(centers)

    output = anhDaPhoiVienA4.copy()
    for (cx, cy) in centers:
        cv2.circle(output, (cx, cy), 5, (0, 255, 255), -1)

    _show_debug_image("cac moc dinh vi", imutils.resize(output, height=750))

    pts_4 = np.array(centers, dtype="float32")
    if len(pts_4) == 0:
        print("KHONG DU CAC MOC 4 MOC DINH VI, CHI TIM DUOC", len(pts_4), "moc")
        return None

    return chuanHoa_LeA4(pts_4)


def PhoiCanh(pst, original_img):
    (tl, tr, br, bl) = pst

    widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
    widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB))

    heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
    heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB))

    dst = np.array(
        [
            [0 - 8, 0 - 8],
            [maxWidth + 8, 0 - 8],
            [maxWidth + 8, maxHeight + 8],
            [0 - 8, maxHeight + 8],
        ],
        dtype="float32",
    )

    matrix = cv2.getPerspectiveTransform(pst, dst)
    warped = cv2.warpPerspective(original_img, matrix, (maxWidth, maxHeight))
    _show_debug_image("anh da phoi canh", imutils.resize(warped, height=750))
    return warped


def XuLyAnh(img_path):
    imgcopy = cv2.imread(img_path)
    if imgcopy is None:
        raise ValueError(f"Khong doc duoc anh tu duong dan: {img_path}")

    resized = imutils.resize(imgcopy, height=1280)
    mocDinhVi = Tim_4_Moc_Dinh_Vi2(resized)
    if mocDinhVi is None or len(mocDinhVi) < 4:
        raise ValueError("Khong tim duoc du 4 moc dinh vi tren anh.")

    warped = PhoiCanh(mocDinhVi, resized)
    Tim_4_Moc_Dinh_Vi2(warped, gioihan_duoi=200, gioihan_tren=700)

    h, w = warped.shape[:2]
    sbd_x1 = int(0.1 * w)
    sbd_y1 = int(0.27 * h)
    sbd_x2 = int(0.36 * w)
    sbd_y2 = int(0.6 * h)

    cv2.rectangle(
        warped,
        (sbd_x1, sbd_y1),
        (sbd_x2, sbd_y2),
        (0, 255, 0),
        2,
    )

    _show_debug_image("SBD REGION", imutils.resize(warped, height=750))
    return warped


if __name__ == "__main__":
    ket_qua = XuLyAnh("now.jpg")
    print(ket_qua.shape)
