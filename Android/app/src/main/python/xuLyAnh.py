import itertools
import json
import cv2
import numpy as np

DEBUG_WINDOWS = False

#hàm resize ảnh
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


def ChuanHoaMaDe(ma_de):
    if ma_de is None:
        return None

    ma_de_str = "".join(str(ma_de).split()).upper()
    if ma_de_str.isdigit():
        return ma_de_str.zfill(3)
    return ma_de_str


def ChuanHoaChuoiDapAn(chuoi_dap_an):
    if chuoi_dap_an is None:
        return None

    return "".join(str(chuoi_dap_an).split()).upper()


def ChuanHoaBoDapAn(bo_dap_an):
    bo_dap_an_chuan = {}

    for ma_de, dap_an in bo_dap_an.items():
        ma_de_chuan = ChuanHoaMaDe(ma_de)
        dap_an_chuan = ChuanHoaChuoiDapAn(dap_an)

        if not ma_de_chuan or not dap_an_chuan:
            continue

        bo_dap_an_chuan[ma_de_chuan] = dap_an_chuan

    return bo_dap_an_chuan

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
        81,
        20,
    )
    return thresh


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
    if pts is None:
        print("KHONG CO DANH SACH MOC NHO")
        return None

    pts = np.array(pts, dtype="float32")
    if len(pts) < 6:
        print("KHONG DU 6 MOC NHO, hien co:", len(pts))
        return None

    # Neu tim du hon 6 moc, chon 6 moc tao thanh 3 hang - 2 cot on dinh nhat.
    if len(pts) > 6:
        to_hop_tot_nhat = None
        diem_tot_nhat = None

        for to_hop in itertools.combinations(pts, 6):
            pts_to_hop = np.array(to_hop, dtype="float32")
            pts_sorted_y = pts_to_hop[np.argsort(pts_to_hop[:, 1])]
            row_top = pts_sorted_y[0:2][np.argsort(pts_sorted_y[0:2, 0])]
            row_mid = pts_sorted_y[2:4][np.argsort(pts_sorted_y[2:4, 0])]
            row_bot = pts_sorted_y[4:6][np.argsort(pts_sorted_y[4:6, 0])]

            trai = np.array([row_top[0], row_mid[0], row_bot[0]])
            phai = np.array([row_top[1], row_mid[1], row_bot[1]])

            do_lech_y_theo_hang = (
                    abs(row_top[0][1] - row_top[1][1]) +
                    abs(row_mid[0][1] - row_mid[1][1]) +
                    abs(row_bot[0][1] - row_bot[1][1])
            )
            do_lech_x_theo_cot = np.std(trai[:, 0]) + np.std(phai[:, 0])
            khoang_cach_hang = np.diff([
                np.mean(row_top[:, 1]),
                np.mean(row_mid[:, 1]),
                np.mean(row_bot[:, 1]),
            ])

            if np.any(khoang_cach_hang <= 0):
                continue

            chieu_rong_hang = np.array([
                row_top[1][0] - row_top[0][0],
                row_mid[1][0] - row_mid[0][0],
                row_bot[1][0] - row_bot[0][0],
                ])
            if np.any(chieu_rong_hang <= 0):
                continue

            diem = (
                    do_lech_y_theo_hang * 3
                    + do_lech_x_theo_cot * 2
                    + np.std(chieu_rong_hang)
                    + np.std(khoang_cach_hang)
            )

            if diem_tot_nhat is None or diem < diem_tot_nhat:
                diem_tot_nhat = diem
                to_hop_tot_nhat = np.vstack([row_top, row_mid, row_bot])

        if to_hop_tot_nhat is None:
            print("KHONG CHON DUOC 6 MOC NHO ON DINH TU DANH SACH:", pts.tolist())
            return None

        pts = to_hop_tot_nhat

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
def Tim_4_Moc_Dinh_Vi2(anh, gioihan_duoi=200, gioihan_tren=1000, tra_tam=False):

    gray = cv2.cvtColor(anh, cv2.COLOR_BGR2GRAY)
    tuongphan = cv2.convertScaleAbs(gray, alpha=1.8, beta=-80)
    # cv2.imshow("tuong phan", resize_keep_ratio(tuongphan.copy(), height=750))
    # cv2.waitKey(0)

    blur = cv2.GaussianBlur(tuongphan, (5, 5), 0)

    thresh = cv2.adaptiveThreshold(
        blur,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV,
        81,
        30,
    )

    # tạm tắt
    # cv2.imshow("adaptive threshold", resize_keep_ratio(thresh.copy(), height=750))
    # cv2.waitKey(0)

    cnts = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]

    centers = []

    for contour in cnts:
        area = cv2.contourArea(contour) #tính diện tích contour

        if not (gioihan_duoi < area < gioihan_tren):
            continue

        hull = cv2.convexHull(contour) #
        hull_area = cv2.contourArea(hull) #

        if hull_area == 0:
            continue

        solidity = float(area) / hull_area
        if solidity < 0.8:
            continue

        peri = cv2.arcLength(contour, True) #tính chu vi contour
        approx = cv2.approxPolyDP(contour, 0.02 * peri, True) #xấp xỉ đa giác

        if not (4 <= len(approx) <= 6): #chỉ chấp nhận contour có 4 hoặc 5 điểm xấp xỉ
            continue

        moments = cv2.moments(approx) # tính moment của contour để tìm tâm
        if moments["m00"] == 0:
            continue

        cx = int(moments["m10"] / moments["m00"]) #tính tọa độ x của tâm contour
        cy = int(moments["m01"] / moments["m00"]) #tính tọa độ y của tâm contour

        centers.append((cx, cy))

    print("TOA DO CAC MOC DINH VI TIM DC:")
    print(centers)
    print("---------------------------------")

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


#hàm tính điểm số của một cell SBD bằng cách đếm số pixel trắng (255) trong cell đó, trả về điểm số
def tinh_score_o(cell):
    score = cv2.countNonZero(cell)
    return score


def PhanLoaiCotTo(score_theo_hang, nguong_khong_to=800, nguong_to_nhieu=700):
    """
    Phan loai 1 cot SBD / Ma de.

    Tra ve:
    - "single", chi_so_hang: cot co 1 o duoc to ro rang
    - "empty", None: cot khong co o nao duoc to
    - "multi", None: cot co nhieu o duoc to hoac qua kho phan biet

    NOTE:
    - nguong_khong_to dung de nhan dien cot bi bo trong
    - nguong_to_nhieu dung de nhan dien cot to nhieu o
    """
    score_sap_xep = sorted(score_theo_hang, reverse=True)
    max1 = score_sap_xep[0]
    max2 = score_sap_xep[1]

    if max1 < nguong_khong_to:
        return "empty", None

    if max2 > nguong_to_nhieu or (max2 != 0 and max1 / max2 < 2):
        return "multi", None

    chi_so_hang = score_theo_hang.index(max1)
    return "single", chi_so_hang

def PhanLoaiHangTo_DapAn(score_theo_cot, nguong_khong_to=800, nguong_to_nhieu=700):
    """
    Phan loai 1 cot SBD / Ma de.

    Tra ve:
    - "single", chi_so_hang: cot co 1 o duoc to ro rang
    - "empty", None: cot khong co o nao duoc to
    - "multi", None: cot co nhieu o duoc to hoac qua kho phan biet

    NOTE:
    - nguong_khong_to dung de nhan dien cot bi bo trong
    - nguong_to_nhieu dung de nhan dien cot to nhieu o
    """
    score_sap_xep = sorted(score_theo_cot, reverse=True)
    max1 = score_sap_xep[0]
    max2 = score_sap_xep[1]

    if max1 < nguong_khong_to:
        return "empty", None

    if max2 > nguong_to_nhieu or (max2 != 0 and max1 / max2 < 2):
        return "multi", None

    chi_so_cot = score_theo_cot.index(max1)
    return "single", chi_so_cot


def Debug_SBD_MaDe(
        warped,
        roi_goc,
        roi_da_resize,
        roi_x1,
        roi_y1,
        ds_score_theo_cot,
        nguong_khong_to=800,
        nguong_to_nhieu=700,
):
    """
    Ve debug cho vung SBD / Ma de len anh warped.

    Cach ve:
    - Neu 1 cot co duy nhat 1 o to hop le: ve hinh tron xanh la.
    - Neu cot khong to hoac to nhieu o: ve hinh chu nhat do quanh ca cot.

    NOTE cho newbie:
    - Viec cham diem dang lam tren anh ROI da resize.
    - Khi ve len anh goc, phai doi toa do tu ROI resize -> ROI goc -> warped.
    """
    if len(ds_score_theo_cot) == 0:
        return warped

    roi_h_goc, roi_w_goc = roi_goc.shape[:2]
    roi_h_resize, roi_w_resize = roi_da_resize.shape[:2]

    ti_le_x = roi_w_goc / float(roi_w_resize)
    ti_le_y = roi_h_goc / float(roi_h_resize)

    so_cot = len(ds_score_theo_cot)
    chieu_rong_cot_resize = roi_w_resize / float(so_cot)
    chieu_cao_hang_resize = roi_h_resize / 10.0

    for j, score_theo_hang in enumerate(ds_score_theo_cot):
        trang_thai, chi_so_hang = PhanLoaiCotTo(
            score_theo_hang,
            nguong_khong_to=nguong_khong_to,
            nguong_to_nhieu=nguong_to_nhieu,
        )

        if trang_thai == "single":
            tam_x_resize = (j + 0.4) * chieu_rong_cot_resize
            tam_y_resize = (chi_so_hang + 0.4) * chieu_cao_hang_resize

            tam_x_goc = int(round(roi_x1 + tam_x_resize * ti_le_x))
            tam_y_goc = int(round(roi_y1 + tam_y_resize * ti_le_y))

            # ban_kinh = int(
            #     min(chieu_rong_cot_resize * ti_le_x, chieu_cao_hang_resize * ti_le_y) * 0.3
            # )
            # ban_kinh = max(8, ban_kinh)

            cv2.circle(warped, (tam_x_goc, tam_y_goc), 12, (0, 255, 0), 3)
        else:
            x1_resize = j * chieu_rong_cot_resize
            x2_resize = (j + 1) * chieu_rong_cot_resize

            x1_goc = int(round(roi_x1 + x1_resize * ti_le_x))
            x2_goc = int(round(roi_x1 + x2_resize * ti_le_x))
            y1_goc = int(round(roi_y1))
            y2_goc = int(round(roi_y1 + roi_h_goc))

            cv2.rectangle(warped, (x1_goc, y1_goc), (x2_goc, y2_goc), (0, 0, 255), 2)

    return warped

def Debug_DapAn(
        warped,
        roi_goc,
        roi_da_resize,
        roi_x1,
        roi_y1,
        ds_score_theo_hang,
        dap_an_thi_sinh=None,
        dap_an_dung=None,
        nguong_khong_to=800,
        nguong_to_nhieu=700,
):
    """
    Ve debug cho vung Dap An len anh warped.

    Cach ve:
    - Neu thi sinh to dung: khoanh xanh o duoc chon.
    - Neu thi sinh to sai: khoanh do o thi sinh to va khoanh xanh o dap an dung.
    - Neu bo trong / to nhieu o: ve khung do quanh ca hang va khoanh xanh o dap an dung.

    NOTE cho newbie:
    - Vung dap an duoc chia thanh 10 hang cau hoi va 4 cot dap an A/B/C/D.
    - ds_score_theo_hang la danh sach 10 hang, moi hang gom 4 score.
    - chi_so_cot = 0,1,2,3 tuong ung A,B,C,D.
    - Viec cham diem dang lam tren anh ROI da resize.
    - Khi ve len anh goc, phai doi toa do tu ROI resize -> ROI goc -> warped.
    """
    if len(ds_score_theo_hang) == 0:
        return warped

    roi_h_goc, roi_w_goc = roi_goc.shape[:2] #lấy ra chiều cao, rộng của vùng đáp án ROI
    roi_h_resize, roi_w_resize = roi_da_resize.shape[:2] #lấy ra chiều cao, rộng của vùng đáp án ROI resize

    ti_le_x = roi_w_goc / float(roi_w_resize)
    ti_le_y = roi_h_goc / float(roi_h_resize)

    so_hang = len(ds_score_theo_hang)
    chieu_rong_cot_resize = roi_w_resize / 4.0
    chieu_cao_hang_resize = roi_h_resize / float(so_hang) #10 hàng

    map_dap_an = {"A": 0, "B": 1, "C": 2, "D": 3}

    def lay_tam_o(chi_so_hang, chi_so_cot):
        tam_x_resize = (chi_so_cot + 0.5) * chieu_rong_cot_resize
        tam_y_resize = (chi_so_hang + 0.5) * chieu_cao_hang_resize

        tam_x_goc = int(round(roi_x1 + tam_x_resize * ti_le_x))
        tam_y_goc = int(round(roi_y1 + tam_y_resize * ti_le_y))
        return tam_x_goc, tam_y_goc

    def ve_vong_tron(chi_so_hang, chi_so_cot, mau, do_day=3):
        tam_x_goc, tam_y_goc = lay_tam_o(chi_so_hang, chi_so_cot)
        cv2.circle(warped, (tam_x_goc, tam_y_goc), 12, mau, do_day)

    for chi_so_hang, score_theo_cot in enumerate(ds_score_theo_hang):
        trang_thai, chi_so_cot = PhanLoaiHangTo_DapAn(
            score_theo_cot,
            nguong_khong_to=nguong_khong_to,
            nguong_to_nhieu=nguong_to_nhieu,
        )

        if trang_thai != "single": #nếu tô ko hợp lệ thì vẽ ô vuông
            x1_resize = 0
            x2_resize = roi_w_resize
            y1_resize = chi_so_hang * chieu_cao_hang_resize
            y2_resize = (chi_so_hang + 1) * chieu_cao_hang_resize

            x1_goc = int(round(roi_x1 + x1_resize * ti_le_x))
            x2_goc = int(round(roi_x1 + x2_resize * ti_le_x))
            y1_goc = int(round(roi_y1 + y1_resize * ti_le_y))
            y2_goc = int(round(roi_y1 + y2_resize * ti_le_y))

            #vẽ ô vuông
            # cv2.rectangle(warped, (x1_goc, y1_goc), (x2_goc, y2_goc), (0, 0, 255), 2)

        dap_an_thi_sinh_hang = None
        if dap_an_thi_sinh is not None and chi_so_hang < len(dap_an_thi_sinh):
            dap_an_thi_sinh_hang = str(dap_an_thi_sinh[chi_so_hang]).upper()

        dap_an_dung_hang = None
        if dap_an_dung is not None and chi_so_hang < len(dap_an_dung):
            dap_an_dung_hang = str(dap_an_dung[chi_so_hang]).upper()

        cot_thi_sinh = map_dap_an.get(dap_an_thi_sinh_hang)
        cot_dap_an_dung = map_dap_an.get(dap_an_dung_hang)
        ##


        if cot_thi_sinh is not None and cot_thi_sinh == cot_dap_an_dung: #nếu đáp án thí sinh chọn đúng - xanh
            ve_vong_tron(chi_so_hang, cot_thi_sinh, (0, 255, 0), 3)
            x_goc, y_goc = lay_tam_o(chi_so_hang, cot_thi_sinh)

            cv2.putText(warped, "+0.25", (x_goc, y_goc), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

            #thêm lưu câu đúng

            continue

        if cot_thi_sinh is not None:
            ve_vong_tron(chi_so_hang, cot_thi_sinh, (0, 0, 255), 3) #nếu đáp án thí sinh chọn sai - đỏ
            # #thêm lưu câu sai

        if cot_dap_an_dung is not None: #vẫn vẽ lên chổ đáp án đúng
            ve_vong_tron(chi_so_hang, cot_dap_an_dung, (255, 255, 0), 3)

    return warped #thêm return câu đúng sai

def XuLySOBAODANH(cacMocNho, warped):
    print("***DEBUG SBD***")
    copy = warped.copy()

    top_left, top_right, mid_left, mid_right, bot_left, bot_right = cacMocNho

    col_gap = top_right[0] - top_left[0]
    row_gap = mid_left[1] - top_left[1]

    sbd_x1 = int(top_left[0] - 1.05 * col_gap)
    sbd_y1 = int(top_left[1] + 0.05 * row_gap)
    sbd_x2 = int(top_left[0] - 0.065 * col_gap)
    sbd_y2 = int(mid_left[1] - 0.048 * row_gap)

    h, w = warped.shape[:2]
    sbd_x1 = max(0, min(w - 1, sbd_x1))
    sbd_y1 = max(0, min(h - 1, sbd_y1))
    sbd_x2 = max(0, min(w - 1, sbd_x2))
    sbd_y2 = max(0, min(h - 1, sbd_y2))

    sbd_roi = copy[sbd_y1:sbd_y2, sbd_x1:sbd_x2]
    cv2.rectangle(warped, (sbd_x1, sbd_y1), (sbd_x2, sbd_y2), (0, 255, 0), 2)

    thresholded_sbd_roi = TienXuLyBanDau(sbd_roi)
    resized_sbd_roi = resize_keep_ratio(thresholded_sbd_roi, height=1000)

    chia_6_cot = resized_sbd_roi.shape[1] // 6
    ds_cot = []
    for j in range(6):
        cot_j = resized_sbd_roi[
            0:resized_sbd_roi.shape[0],
            chia_6_cot * j:chia_6_cot * (j + 1),
        ]
        ds_cot.append(cot_j)

    chia_10_hang = resized_sbd_roi.shape[0] // 10
    SBD = []
    ds_score_theo_cot = []

    for j, cot in enumerate(ds_cot):
        score_theoHang = []

        for i in range(10):
            hang_i = cot[chia_10_hang * i:chia_10_hang * (i + 1), 0:cot.shape[1]]
            score = tinh_score_o(hang_i)
            score_theoHang.append(score)

        ds_score_theo_cot.append(score_theoHang)

        score_theoHang_copy = sorted(score_theoHang, reverse=True)
        max1 = score_theoHang_copy[0]
        max2 = score_theoHang_copy[1]
        print(f"COT {j+1}: max1 = {max1}, max2 = {max2}")

        trang_thai, chi_so_hang = PhanLoaiCotTo(score_theoHang)

        if trang_thai == "empty":
            print(f"COT {j+1} ==> KHONG CO O NAO DUOC TO")
            SBD.append("X")
            continue

        if trang_thai == "multi":
            print(f"COT {j+1} ==> CO 2 O DUOC TO, CAN XET TIEP")
            SBD.append("X")
            continue

        SBD.append(chi_so_hang)
        print(f"COT {j+1} ==> O DUOC TO LA: {chi_so_hang}")

    warped = Debug_SBD_MaDe(
        warped=warped,
        roi_goc=sbd_roi,
        roi_da_resize=resized_sbd_roi,
        roi_x1=sbd_x1,
        roi_y1=sbd_y1,
        ds_score_theo_cot=ds_score_theo_cot,
    )

    print("SBD:", SBD)
    sbd_str = "".join(str(num) for num in SBD)
    print("SBD (chuoi):", sbd_str)
    print("")

    cv2.putText(warped, f"SBD: {sbd_str}", (10, 100), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (255,255,255), 2)

    return sbd_str, warped



def XuLyMADE(cacMocNho, warped):
    print("***DEBUG MA DE***")
    copy = warped.copy()

    top_left, top_right, mid_left, mid_right, bot_left, bot_right = cacMocNho

    col_gap = top_right[0] - top_left[0]
    row_gap = mid_left[1] - top_left[1]

    made_x1 = int(top_left[0] + 0.1 * col_gap)
    made_y1 = int(top_left[1] + 0.05 * row_gap)
    made_x2 = int(top_left[0] + 0.61 * col_gap)
    made_y2 = int(mid_left[1] - 0.048 * row_gap)

    h, w = warped.shape[:2]
    made_x1 = max(0, min(w - 1, made_x1))
    made_y1 = max(0, min(h - 1, made_y1))
    made_x2 = max(0, min(w - 1, made_x2))
    made_y2 = max(0, min(h - 1, made_y2))

    made_roi = copy[made_y1:made_y2, made_x1:made_x2]
    print("kich thuoc MA DE _ ROI:", made_roi.shape)

    thresholded_made_roi = TienXuLyBanDau(made_roi)
    resized_made_roi = resize_keep_ratio(thresholded_made_roi, height=1000)
    print("kich thuoc MA DE _ ROI sau khi xu ly:", resized_made_roi.shape)

    cv2.rectangle(warped, (made_x1, made_y1), (made_x2, made_y2), (0, 255, 0), 2)

    ##XỬ LÝ-----------------------------------

    chia_3_cot = resized_made_roi.shape[1] // 3
    ds_cot = []
    for j in range(3):
        cot_j = resized_made_roi[
            0:resized_made_roi.shape[0],
            chia_3_cot * j:chia_3_cot * (j + 1),
        ]
        ds_cot.append(cot_j)

    chia_10_hang = resized_made_roi.shape[0] // 10
    MADE = []
    ds_score_theo_cot = []

    for j, cot in enumerate(ds_cot):
        score_theoHang = []

        for i in range(10):
            hang_i = cot[chia_10_hang * i:chia_10_hang * (i + 1), 0:cot.shape[1]]
            score = tinh_score_o(hang_i)
            score_theoHang.append(score)

        ds_score_theo_cot.append(score_theoHang)

        score_theoHang_copy = sorted(score_theoHang, reverse=True)
        max1 = score_theoHang_copy[0]
        max2 = score_theoHang_copy[1]
        print(f"COT {j+1}: max1 = {max1}, max2 = {max2}")

        trang_thai, chi_so_hang = PhanLoaiCotTo(score_theoHang)

        if trang_thai == "empty":
            print(f"COT {j+1} ==> KHONG CO O NAO DUOC TO")
            MADE.append("X")
            continue

        if trang_thai == "multi":
            print(f"COT {j+1} ==> CO 2 O DUOC TO, CAN XET TIEP")
            MADE.append("N")
            continue

        MADE.append(chi_so_hang)
        print(f"COT {j+1} ==> O DUOC TO LA: {chi_so_hang}")

    warped = Debug_SBD_MaDe(
        warped=warped,
        roi_goc=made_roi,
        roi_da_resize=resized_made_roi,
        roi_x1=made_x1,
        roi_y1=made_y1,
        ds_score_theo_cot=ds_score_theo_cot,
    )

    print("MA DE:", MADE)
    made_str = "".join(str(num) for num in MADE)
    print("MA DE (chuoi):", made_str)
    print("")

    cv2.putText(warped, f"MA DE: {made_str}", (10, 140), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (255, 0, 0), 2)

    return made_str, warped


def XuLyDAPAN(cacMocNho, warped, MaDeThiSinh, BoDapAn, cau1_10 = False, cau11_20 = False, cau21_30 = False, cau31_40 = False):
    #PHAN VUNG ----------------------------
    copy = warped.copy()
    #cacMocNho phai được chuẩn hóa bang ham chuanHoa_6_MocNho()
    #warped là ảnh đã được phối cảnh 4 mốc lớn, có kích thước chuẩn để tính toán chính xác vị trí SBD

    top_left, top_right, mid_left, mid_right, bot_left, bot_right = cacMocNho

    ds_nhom_duoc_chon = [cau1_10, cau11_20, cau21_30, cau31_40]
    so_nhom_duoc_chon = sum(bool(x) for x in ds_nhom_duoc_chon)
    if so_nhom_duoc_chon == 0:
        cau1_10 = True
    elif so_nhom_duoc_chon > 1:
        raise ValueError("XuLyDAPAN chi duoc xu ly 1 nhom cau trong moi lan goi.")

    # Tính khoảng cách giữa các mốc để xác định kích thước và vị trí vùng SBD
    col_gap = top_right[0] - top_left[0]
    row_gap = mid_left[1] - top_left[1]

    if(cau1_10==True):
        # NOTE: nho vi tri bat dau de sau nay doi hang 0..9 thanh cau 1..10
        cau_bat_dau = 1

        dapan_x1 = int(top_right[0] + 0.1 * col_gap)#
        dapan_y1 = int(top_right[1] + 0.05 * row_gap)

        dapan_x2 = int(top_right[0] + 0.77 * col_gap) #
        dapan_y2 = int(mid_right[1] - 0.048 * row_gap)


    elif(cau11_20==True):
        # NOTE: nhom thu 2 tuong ung cau 11..20
        cau_bat_dau = 11
        dapan_x1 = int(mid_left[0] - 0.9 * col_gap)#
        dapan_y1 = int(mid_left[1] + 0.05 * row_gap)

        dapan_x2 = int(mid_left[0] - 0.23 * col_gap) #
        dapan_y2 = int(bot_left[1] - 0.048 * row_gap)

    elif(cau21_30==True):
        #
        # NOTE: nhom thu 3 tuong ung cau 21..30
        cau_bat_dau = 21
        dapan_x1 = int(mid_left[0] + 0.1 * col_gap)#
        dapan_y1 = int(mid_left[1] + 0.05 * row_gap)

        dapan_x2 = int(mid_left[0] + 0.77 * col_gap) #
        dapan_y2 = int(bot_left[1] - 0.048 * row_gap)

    elif(cau31_40==True):
        # NOTE: nhom thu 4 tuong ung cau 31..40
        cau_bat_dau = 31
        dapan_x1 = int(mid_right[0] + 0.1 * col_gap)#
        dapan_y1 = int(mid_right[1] + 0.05 * row_gap)

        dapan_x2 = int(mid_right[0] + 0.77 * col_gap) #
        dapan_y2 = int(bot_right[1] - 0.048 * row_gap)


    h, w = warped.shape[:2]
    dapan_x1 = max(0, min(w - 1, dapan_x1)) #trên trái
    dapan_y1 = max(0, min(h - 1, dapan_y1))

    dapan_x2 = max(0, min(w - 1, dapan_x2)) # dưới phải
    dapan_y2 = max(0, min(h - 1, dapan_y2))

    dapan_roi = copy[dapan_y1:dapan_y2, dapan_x1:dapan_x2] #cắt vùng Dap an từ ảnh đã phối cảnh để xử lý riêng

    thresholded_dapan_roi = TienXuLyBanDau(dapan_roi) #đưa vùng Dap an về ảnh nhị phân
    #resize vùng Dap an về chiều cao 1000
    resized_dapan_roi = resize_keep_ratio(thresholded_dapan_roi, height=1000)

    # cv2.imshow("vung so bao danh nhi phan", resize_keep_ratio(resized_dapan_roi, height=750))
    # cv2.waitKey(0)

    #vẽ hình chữ nhật vùng MA DE lên ảnh để debug
    cv2.rectangle(warped, (dapan_x1, dapan_y1), (dapan_x2, dapan_y2), (0, 255, 0), 2)

    #XU LY-----------------------------------
    chia_10_hang = resized_dapan_roi.shape[0] // 10
    ds_hang = []
    for j in range(10):
        hang_j = resized_dapan_roi[ chia_10_hang * j:chia_10_hang * (j + 1), 0:resized_dapan_roi.shape[1] ]
        ds_hang.append(hang_j)
        # cv2.imshow(f"hang{j+1}", resize_keep_ratio(hang_j,width=750, height=750))
        # cv2.waitKey(0)
        # cv2.destroyAllWindows()

    chia_4_cot = resized_dapan_roi.shape[1] // 4
    DAPAN = []
    ds_score_theo_hang = []

    for j, hang in enumerate(ds_hang):
        score_theoCot = [] #để xác định tô câu nào

        # Moi hang chi co 4 o dap an A/B/C/D, nen can chia theo chieu rong.
        for i in range(4):
            cot_i = hang[0:hang.shape[0], chia_4_cot * i:chia_4_cot * (i + 1)]
            score = tinh_score_o(cot_i)
            score_theoCot.append(score)

        ds_score_theo_hang.append(score_theoCot)

        score_theoCot_copy = sorted(score_theoCot, reverse=True)
        max1 = score_theoCot_copy[0]
        max2 = score_theoCot_copy[1]
        print(f"Hang {j+1}: max1 = {max1}, max2 = {max2}")

        trang_thai, chi_so_cot = PhanLoaiHangTo_DapAn(score_theoCot)
        if trang_thai == "single":
            print(f"Hang {j+1} ==> O DUOC TO LA: {chi_so_cot}")
            DAPAN.append(["A", "B", "C", "D"][chi_so_cot])
            continue

        if trang_thai == "empty":
            print(f"Hang {j+1} ==> KHONG CO O NAO DUOC TO")
            DAPAN.append("X")
            continue

        print(f"Hang {j+1} ==> CO 2 O DUOC TO, CAN XET TIEP")
        DAPAN.append("N")

    print("Ket qua dap an:", DAPAN)
    print("")
    #CHAM DIEM --------------------------------

    made = ChuanHoaMaDe(MaDeThiSinh)
    daAnDung_str = BoDapAn.get(made)

    if daAnDung_str is None:
        ds_ma_de = ", ".join(sorted(BoDapAn.keys()))
        raise ValueError(
            f"Khong tim thay dap an cho ma de '{made}'. "
            f"Ma de doc duoc hien tai la '{MaDeThiSinh}'. "
            f"Cac ma de dang co: {ds_ma_de}"
        )

    daAnDung_str = ChuanHoaChuoiDapAn(daAnDung_str)
    if len(daAnDung_str) != 40:
        raise ValueError(
            f"Bo dap an cua ma de '{made}' khong hop le: can 40 ky tu A/B/C/D, "
            f"nhung hien co {len(daAnDung_str)} ky tu."
        )

    dapAnDung_list = list(daAnDung_str)
    score = 0

    if cau1_10 == True:
        dap_an_dung_nhom = dapAnDung_list[0:10] #lấy đáp án của 10 câu đầu
        #gọi hàm chấm điểm mỗi 10 câu
        diemThiSinh, soCauDung = ChamDiem(DAPAN, dap_an_dung_nhom) #chấm điểm 10 câu

    elif cau11_20 == True:
        dap_an_dung_nhom = dapAnDung_list[10:20]
        diemThiSinh, soCauDung = ChamDiem(DAPAN, dap_an_dung_nhom)

    elif cau21_30 == True:
        dap_an_dung_nhom = dapAnDung_list[20:30]
        diemThiSinh, soCauDung = ChamDiem(DAPAN, dap_an_dung_nhom)

    elif cau31_40 == True:
        dap_an_dung_nhom = dapAnDung_list[30:40]
        diemThiSinh, soCauDung = ChamDiem(DAPAN, dap_an_dung_nhom)

    print(
        f"Nhom cau {cau_bat_dau}-{cau_bat_dau + 9} | "
        f"Dap an dung: {''.join(dap_an_dung_nhom)} | "
        f"Dap an doc duoc: {''.join(DAPAN)}"
    )

    # NOTE: giu lai chi tiet tung cau o dang list[dict] cho de debug va de XuLyAnh ghep du 40 cau.
    chi_tiet_10_cau = []
    for i, dap_an_thi_sinh in enumerate(DAPAN):
        cau_so = cau_bat_dau + i
        dap_an_dung = dap_an_dung_nhom[i]

        # NOTE: X = bo trong, N = to nhieu o; ca hai deu xem la khong hop le.
        if dap_an_thi_sinh == dap_an_dung:
            trang_thai_cau = "dung"
        elif dap_an_thi_sinh in ("X", "N"):
            trang_thai_cau = "khong_hop_le"
        else:
            trang_thai_cau = "sai"

        chi_tiet_10_cau.append({
            "cauSo": cau_so,
            "dapAnThiSinh": dap_an_thi_sinh,
            "trangThai": trang_thai_cau,
        })
    # --------------------------------------------

    warped = Debug_DapAn(
        warped=warped, # ảnh phiếu
        roi_goc=dapan_roi, #ảnh vùng đáp án ROI
        roi_da_resize=resized_dapan_roi, # ảnh vùng đáp án ROI resize
        roi_x1=dapan_x1,
        roi_y1=dapan_y1,
        ds_score_theo_hang=ds_score_theo_hang,
        dap_an_thi_sinh=DAPAN,
        dap_an_dung=dap_an_dung_nhom,
    )

    return diemThiSinh, soCauDung, warped, chi_tiet_10_cau

#Hàm chấm điểm mỗi 10 câu, so sánh đáp án thí sinh với đáp án đúng để tính điểm
def ChamDiem(dapAn_ThiSinh_10cau, list_dap_an_dung):
    score = 0
    soCauDung = 0
    for i in range(len(dapAn_ThiSinh_10cau)):
        if dapAn_ThiSinh_10cau[i] == list_dap_an_dung[i]:
            score += 0.25
            soCauDung += 1

    print(f"Diem: {score}/10")

    return score, soCauDung

def lay_HoTen_Lop(warped):
    copy = warped.copy()

    w = warped.shape[1]
    h = warped.shape[0]

    #Ho ten
    x1 = int(w * 0.29)
    y1 = int(h * 0.006)

    x2 = int(w * 0.66)
    y2 = int(h * 0.07)

    hoten_roi = copy[y1:y2, x1:x2]
    cv2.rectangle(warped, (x1, y1), (x2, y2), (0, 0, 255), 2)

    #LOP
    x1 = int(w * 0.68)
    y1 = int(h * 0.006)

    x2 = int(w * 1)
    y2 = int(h * 0.07)

    lop_roi = copy[y1:y2, x1:x2]
    cv2.rectangle(warped, (x1, y1), (x2, y2), (255, 0, 0), 2)


    return hoten_roi, lop_roi, warped


def XuLyAnh(img_path, BoDapAn):

    # Cách nhanh nhất là gọi dict() nếu nó có interface Map
    if not isinstance(BoDapAn, dict):
        try:
            # Chuyển đổi HashMap thành dict
            BoDapAn = dict(BoDapAn)
        except Exception:
            # Nếu dict() trực tiếp không được, lặp qua entrySet của Java
            py_dict = {}
            iterator = BoDapAn.entrySet().iterator()
            while iterator.hasNext():
                entry = iterator.next()
                py_dict[entry.getKey()] = entry.getValue()
            BoDapAn = py_dict

    BoDapAn = ChuanHoaBoDapAn(BoDapAn)
    if not BoDapAn:
        raise ValueError("BoDapAn rong hoac khong the chuan hoa thanh dict hop le.")

    for ma_de, dap_an in BoDapAn.items():
        if len(dap_an) != 40:
            raise ValueError(
                f"Bo dap an cua ma de '{ma_de}' khong hop le sau chuan hoa: "
                f"can 40 ky tu, nhung hien co {len(dap_an)} ky tu."
            )



    img = cv2.imread(img_path) #đọc ảnh
    if img is None:
        raise ValueError(f"Khong doc duoc anh: {img_path}")

    resized = resize_keep_ratio(img, height=1280) #resize

    moc = Tim_4_Moc_Dinh_Vi2(resized) #tìm vị trí 4 mốc lớn
    if moc is None:
        raise ValueError("Khong tim du 4 moc")

    warped = PhoiCanh(moc, resized) #cắt vào 4 mốc lớn

    #tìm 6 mốc nhỏ
    cacMocNho = Tim_4_Moc_Dinh_Vi2(warped, gioihan_duoi=250, gioihan_tren=700, tra_tam=True)
    so_moc_nho = len(cacMocNho) if cacMocNho is not None else 0
    print(f"SO MOC NHO TIM DUOC: {so_moc_nho}")

    cacMocNho = chuanHoa_6_MocNho(cacMocNho) # sắp xếp 6 mốc nhỏ: TL, TR, ML, MR, BL, BR
    if cacMocNho is None:
        raise ValueError(f"Khong tim du 6 moc nho de xu ly. Hien chi tim duoc {so_moc_nho} moc.")

    #vẽ các mốc nhỏ lên ảnh đã phối cảnh để debug
    i = 0
    for (cx, cy) in cacMocNho:
        cv2.circle(warped, (int(cx), int(cy)), 10, (0, 255, 255), -1)
        cv2.putText(warped, str(i), (int(cx) -10, int(cy) - 20), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
        i += 1

    #khoanh vùng SBD

    ThiSinhID, warped= XuLySOBAODANH(cacMocNho, warped)
    made_str, warped= XuLyMADE(cacMocNho, warped)

    diem1_10, soCauDung1_10,  warped, chi_tiet_1_10 = XuLyDAPAN(cacMocNho, warped, made_str, BoDapAn, cau1_10=True)
    diem11_20, soCauDung11_20, warped, chi_tiet_11_20 = XuLyDAPAN(cacMocNho, warped, made_str, BoDapAn, cau11_20 =True)
    diem21_30, soCauDung21_30, warped, chi_tiet_21_30 = XuLyDAPAN(cacMocNho, warped, made_str, BoDapAn, cau21_30 =True)
    diem31_40, soCauDung31_40, warped, chi_tiet_31_40 = XuLyDAPAN(cacMocNho, warped, made_str, BoDapAn, cau31_40 =True)

    # NOTE: Noi 4 nhom thanh du lieu day du 40 cau, sau do moi doi sang JSON de tra ve cho Java.
    chi_tiet_40_cau = chi_tiet_1_10 + chi_tiet_11_20 + chi_tiet_21_30 + chi_tiet_31_40
    Json_DapAnThiSinh = json.dumps(chi_tiet_40_cau, ensure_ascii=False)

    Ten_ROI, Lop_LOP, warped = lay_HoTen_Lop(warped)


    # Tính câu đúng
    SOCAUDUNG = soCauDung1_10 + soCauDung11_20 + soCauDung21_30 + soCauDung31_40
    cv2.putText(warped, f"Dung: {SOCAUDUNG}/40", (10, 180), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 255), 2)

    #Tính điểm
    TongDiem = diem1_10 + diem11_20 + diem21_30 + diem31_40
    print (f"Tong diem: {TongDiem}/10")
    cv2.putText(warped, f"Tong diem: {TongDiem}/10", (10, 220), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 255), 2)

    # cv2.imshow("DAP AN", resize_keep_ratio(hoten_roi, width=750, height=200))
    # cv2.waitKey(0)

    # cv2.imshow("anh da xu ly", resize_keep_ratio(warped, height=750))
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()

    return warped, Ten_ROI, Lop_LOP, ThiSinhID, made_str, TongDiem, Json_DapAnThiSinh

    #return warped, Ten_ROI, Lop_ROI, ThiSinhID, MaDe, TongDiem, Json_DapAnThiSinh

# BoDapAn = {
#     "001": "ABCB ACBCABCABDBCABDBCABACBDADCADCABDABCA",
#     "002": "BCABCABDBCABDBCABACBDADCADCABDABCACCCABC",
#     "003": "CABDABCAABCBACBCABCABDBCABDBCABACBDADCAD",
#     "004": "DABCAABCBACBCABCABDBCABDBCABACBDADCADCAB",
# }


# xuly, Ten_ROI, Lop_LOP, ThiSinhID, made_str, TongDiem, Json_DapAnThiSinh = XuLyAnh("test8_dapAn.jpg", BoDapAn)


# print(Json_DapAnThiSinh)
