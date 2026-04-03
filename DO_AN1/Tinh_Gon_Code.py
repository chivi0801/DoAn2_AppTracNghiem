# A. PHẦN PHỐI CẢNH

#CÁC BƯỚC:
# 1. đọc ảnh 
# 2. Nhị phân hóa.
# 3. Phát hiện cạnh (lề) giấy A4.
# 4. phối lại cảnh ( nhờ 4 góc giấy)
# 5. phối lại các nút định vị lớn 1 lần nửa

#------------------------------------------------------------------------------------------------------------------
def xoayAnh_veDoc(img):

    h = img.shape[0]    # chiều cao ảnh
    w = img.shape[1]    # chiều rộng

    if w > h:  #so sánh nếu rộng > cao (tức ảnh đang nằm ngang) thì xoay dọc lại 

        rotated_img = cv2.rotate(img, cv2.ROTATE_90_CLOCKWISE) #xoay 90 độ  # hoán vị trục , ko phải Affin 2x3

        return rotated_img 
    
    else: #trường hợp ngược lại ko xoay
        return img 
    
#----------------------------------------------------------------------

# 2. Nhị phân hóa.============================================================================================================
def DocVaTienXuLy(img, kernel_hang= 7, kernel_cot= 7):
    
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)  ## độ chói

    copy = imutils.resize(gray.copy(), height = 750)
    cv2.imshow('anh xam', copy)
    cv2.waitKey(0)
    
    tuongphan = cv2.convertScaleAbs(gray, alpha=1.3, beta=-80) ## kéo dãn tương phản tuyến tính
    
    kernel_sharpening = np.array([[0, -0.5, 0], 
                                [-0.5,  3, -0.5], 
                                [0, -0.5, 0]], dtype = np.float32)
        
        
    sacnet  = cv2.filter2D(tuongphan, -1, kernel_sharpening) ##laplace

    copy2 = imutils.resize(sacnet.copy(), height = 750)
    cv2.imshow('tang tuong phan va sac net', copy2)
    cv2.waitKey(0)

    gauss = cv2.GaussianBlur(sacnet, (kernel_hang, kernel_cot), 0) ## Làm mịn Gauss
    copy3 = imutils.resize(gauss.copy(), height = 750)
    cv2.imshow('lam min Gauss', copy3)
    cv2.waitKey(0)

    edged = cv2.Canny(gauss, 50, 150) ## nhị phân hóa Canny

    copy4 = imutils.resize(edged.copy(), height = 750) 
    cv2.imshow('Tim bien Canny', copy4)
    cv2.waitKey(0)
    
    return edged


# 3. phát hiện lề.============================================================================================================

# Tìm các đường bao (contours) của ảnh 
def PhatHienLeA4(img):

    #Tìm đường viền.
    cnts = cv2.findContours(img.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)[:5]

    if len(cnts) == 0:
        return None

    #duyệt qua mỗi đường biên lấy được
    for c in cnts:

        # xấp xỉ đa giác
        peri = cv2.arcLength(c, True) 
        approx = cv2.approxPolyDP(c, 0.04 * peri, True)  #thuật toán Douglas–Peucker (Ramer–Douglas–Peucker).

        # nếu có xấp xỉ khoảng 4 - 6 điểm (dự phòng sai số).
        if 4 <= len(approx) <= 6 :    

            # thì cho rằng đó là đường viền của tờ A4
            screenCnt = approx
            break

        else:
            print(" Khong tim thay duong vien")
            
    h = img.shape[0]
    w = img.shape[1]
    area = h * w

    screenCnt_area = cv2.contourArea(screenCnt) #tính diện tích của đường viền tìm được
    
    # nếu ko có biên hoặc biên nhỏ hơn diện tích 1/3 ảnh gốc(trường hợp bắt nhầm các ô tô thay vì viền A4) 
    # --> cho rằng ảnh có biên A4
    if (screenCnt is None) or (screenCnt_area < area/3): 
        return None                                      
        print('Khong tim duoc vien A4')
        
    # Định hình lại screenCnt từ (4,1,2) ----> (4,2) 
    pst = screenCnt.reshape(4, 2) 

    return pst

# Hàm chuẩn hóa thứ tự các điểm --------------------------------------------------------------------------------------------------------------
def chuanHoa_LeA4(pts):   #hàm chuẩn hóa các góc tài liệu và sắp xếp lại thứ tự 4 điểm

    rect = np.zeros((4, 2), dtype="float32")

    # tổng (x + y) cho mỗi điểm
    s = pts.sum(axis=1)

    # hiệu (y-x) cho mỗi điểm
    diff = np.diff(pts, axis=1)

    # xác định vị trí các góc   # argmin / argmax: trả về index.

    rect[0] = pts[ np.argmin(s) ]        # top-left có tổng nhỏ nhất
    rect[2] = pts[np.argmax(s)]        # bottom-right có tổng lớn nhất

    rect[1] = pts[np.argmin(diff)]     # top-right có hiệu nhỏ nhất
    rect[3] = pts[np.argmax(diff)]     # bottom-left có hiệu lớn nhất

    return rect

# Hàm phối cảnh ---------------------------------------------------------------------------------------------------------------------------
def PhoiCanh(pst, original_img):

    (tl, tr, br, bl) = pst 
    
    # Tính chiều rộng của ảnh mới (Euculic)

    widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2)) 
    widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB)) #lấy cái lớn nhất

    # Tính chiều cao của ảnh mới
    heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
    heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB)) #lấy cái lớn nhất

    #Tạo ra các tọa độ cuối cùng của các điểm tham chiếu.
    dst = np.array([
        [0 -8, 0 -8],                     # top - left
        [maxWidth + 8, 0 -8],             # top - right 

        [maxWidth + 8, maxHeight + 8],    # bottom - right 
        [0-8, maxHeight + 8]              # bottm - left 
    ], dtype="float32")

    # Xác định ma trận để thực hiện phép biến đổi phối cảnh.(perspectiv e transformation)
    M = cv2.getPerspectiveTransform(pst, dst)

    # phép biến đổi phối cảnh.(perspective transformation)
    warped = cv2.warpPerspective(original_img, M, (maxWidth, maxHeight))

    
   
    # kết quả của phép biến đổi phối cảnh.(perspective transformation)
    warped_show = imutils.resize(warped, height=750)

    cv2.imshow("def PhoiCanh() --> anh da phoi canh ", warped_show) 
    cv2.waitKey(0)
    # cv2.destroyAllWindows()
    return warped
    

    
def Ve_Chia_anh_4_phan(img):
    nua_chieu_cao = int(img.shape[0] / 2 ) #lấy 1/2 chiều cao ảnh (2 mốc lớn ở giữa)

    chieu_rong = int(img.shape[1]) # lấy chiều rộng ảnh

    mot_phan4_chieucao= int(nua_chieu_cao /2) # lấy 1/4 chiều cao


    #xét xem tl và tr có < hơn 1/4 chiều cao hay ko? nếu có thì ảnh đang bị lật 180 ==> xoay lại 180 độ.
    anh_da_ve_len = img.copy()
   
        
    cv2.line(anh_da_ve_len, (0,mot_phan4_chieucao), (chieu_rong, mot_phan4_chieucao), (0, 255, 255), 2)

    cv2.line(anh_da_ve_len, (0,nua_chieu_cao), (chieu_rong, nua_chieu_cao), (0, 0, 255), 2)

    cv2.line(anh_da_ve_len, (0,nua_chieu_cao + mot_phan4_chieucao), (chieu_rong,nua_chieu_cao + mot_phan4_chieucao), (0, 255, 0), 2)
    
    return anh_da_ve_len
#//////////////////////////////////////////////////////////////////////////////////////////////////////////////--------------------------

# hàm xoay ảnh về thẳng hàng dựa vào các mốc nhỏ khi đã cắt vào 4 mốc lớn -----------------------------------------------------
def Xoay_Anh_ve_thang_hang(daPhoiCanh_4MocDinhVi, gioihanduoi = 150, gioihantren = 300):  

    #Tìm 4 mốc nhỏ định vị và sắp xếp
    pst4_nho = Tim_4_Moc_Dinh_Vi2(daPhoiCanh_4MocDinhVi, gioihanduoi, gioihantren) 

    #kiem tra -------
    if pst4_nho is None: 
        print("KHONG THE XOAY VI KHONG DU MOC DINH VI")
        print("-----------------")
        return daPhoiCanh_4MocDinhVi
    #-----------------

    tl, tr, br, bl = pst4_nho

    nua_chieu_cao = int(daPhoiCanh_4MocDinhVi.shape[0] / 2 ) #lấy 1/2 chiều cao ảnh (2 mốc lớn ở giữa)
    mot_phan4_chieucao= int(nua_chieu_cao /2) # lấy 1/4 chiều cao
    # ba_phan4_chieucao = nua_chieu_cao + mot_phan4_chieucao

    anhXoay_cuoiCung = daPhoiCanh_4MocDinhVi      

    #vẽ điểm
    for (x,y) in pst4_nho:
         cv2.circle(anhXoay_cuoiCung, (int(x), int(y)), 5, (0, 255, 255), -1)  # -1 = vẽ hình tròn đầy
    
    # xét nếu top-left và top-right nằm trong khoảng 1/4 đầu tiên của phiếu --> phiếu ngược --> xoay 180
    # xét nếu bottom-left và bottom-right nằm trong khoảng 1/4 đầu thứ 3 của phiếu --> phiếu ngược --> xoay 180

    if tl[1] < mot_phan4_chieucao   or  tr[1] < mot_phan4_chieucao:

        anhXoay_cuoiCung = cv2.rotate(daPhoiCanh_4MocDinhVi, cv2.ROTATE_180) 

        
    return anhXoay_cuoiCung




     

# hàm tìm các mốc định vị chuẩn (đã update) --------------------------------------------------------------------------------------------
def Tim_4_Moc_Dinh_Vi2(anhDaPhoiVienA4, gioihan_duoi=200, gioihan_tren=900):

    gray = cv2.cvtColor(anhDaPhoiVienA4, cv2.COLOR_BGR2GRAY)

    tuongphan = cv2.convertScaleAbs(gray, alpha=1.6, beta=-80)
    # cv2.imshow('tuong phan', tuongphan)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()
   
    blur = cv2.GaussianBlur(tuongphan, (5,5), 0)

    # === Adaptive threshold  ===
    thresh = cv2.adaptiveThreshold(
        blur, 255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY_INV,   
        31, 5
    )  # 31: kernel 31x31. 5: hằng số trừ đi của Gauss(C)   (c lớn ngưỡng giảm)

    copy = imutils.resize(thresh.copy(), height = 750)
    cv2.imshow('adaptive threshold', copy)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    # Tìm đường viền các mốc
    cnts = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)


    markers = []

    for c in cnts:
        area = cv2.contourArea(c)

        # 1. Lọc theo diện tích ---
        if not (gioihan_duoi < area < gioihan_tren):
            continue

        # 2. Lọc theo độ đặc (solidity) ---
        hull = cv2.convexHull(c)
        hull_area = cv2.contourArea(hull)  #diện tích đa giác bằng công thức hình học: Công thức shoelace (dây giày)

        if hull_area == 0:
            continue

        solidity = float(area) / hull_area
 
        if solidity < 0.8:
            continue

        # 3. Lọc theo xấp xỉ đa giác (4-5 đỉnh) ---
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)

        if 4 <= len(approx) <= 5:
            markers.append(approx)

    #tính tâm của các mốc
    centers = []

    for m in markers:
        M = cv2.moments(m)
        if M["m00"] != 0:
            cx = int(M["m10"] / M["m00"])  ## moment hinh hoc ( 10 tong x có trọng sô / diện tích)
            cy = int(M["m01"] / M["m00"])
            centers.append((cx, cy))

    print("Toa do cac moc dinh vi tim duoc:")
    print(centers)

    # vẽ điểm
    output = anhDaPhoiVienA4.copy()

    for (cx, cy) in centers:
        cv2.circle(output, (cx, cy), 5, (0, 255, 255), -1)


    # cv2.imshow("Adaptive threshold", imutils.resize(thresh, height=750))
    cv2.imshow("cac moc dinh vi", imutils.resize(output, height=750))
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    # === chuẩn hóa, sắp xếp tl, tr, br, bl ===
    pts_4 = np.array(centers, dtype="float32")

    # Fix 
    if len(pts_4) == 0 : 
        print("KHONG DU CAC MOC 4 MOC DINH VI, CHI TIM DUOC", len(pts_4) , "moc")
        return None

    pts_4 = chuanHoa_LeA4(pts_4)

    return pts_4


# MAIN:@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

import cv2 
import numpy as np
import imutils  
 

#hàm kiểm tra thuộc trường hợp ảnh chụp phiếu có chụp viền hay ko chụp viền và thực thi ------------------------------------------
# (điều kiện ko có viền thì phải thấy các mốc định vị)
def KiemTraAnh_CoVien_KoVien_vaChayChuongTrinh(img):

    bansao_show = imutils.resize(img, height=750)

    cv2.imshow("anh goc", bansao_show)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    bansao = img.copy()

    anhSuDung = img.copy()

    nhi_phan = DocVaTienXuLy(anhSuDung) ## trả về canny

    # nhi_phan_copy = imutils.resize(nhi_phan.copy(), height=750)
    # cv2.imshow('tsss', nhi_phan_copy)
    # cv2.waitKey(0) 
    # cv2.destroyAllWindows()


    # Bước 1: Thử tìm viền giấy A4 (Case 1)
    le_A4 = PhatHienLeA4(nhi_phan)


    if le_A4 is not None:
        print("=> TRUONG HOP 1: co vien A4")

        # Quy trình cũ:
        chuan_hoa = chuanHoa_LeA4(le_A4)
        daPhoiCanhVienA4 = PhoiCanh(chuan_hoa, bansao)
        
        anh_xoayDoc = xoayAnh_veDoc(daPhoiCanhVienA4)
        mocDinhVi = Tim_4_Moc_Dinh_Vi2(anh_xoayDoc)

       

        if mocDinhVi is not None:
            daPhoiCanh_4MocDinhVi = PhoiCanh(mocDinhVi, anh_xoayDoc) ## lúc này ảnh đã phối chuẩn vào các mốc định vị lớn.)
        else:
            print("Loi!!: da cat giay A4 nhung ko tim thay cac moc dinh vi ben trong.")
        
            return anh_xoayDoc

        copy = daPhoiCanh_4MocDinhVi.copy()

        # ve = Ve_Chia_anh_4_phan(daPhoiCanh_4MocDinhVi)
        #xoay anh bi lat 180 do:
        
        final_img = Xoay_Anh_ve_thang_hang(copy, 150, 300) 
        output = imutils.resize(final_img, height=750)
        cv2.imshow('cuoi cung xoay ve thang hang', output)
        cv2.waitKey(0) 
        cv2.destroyAllWindows()

        return final_img

    else:
        print("=> TRUONG HOP 2: ko co vien A4")

        mocDinhVi = Tim_4_Moc_Dinh_Vi2(bansao, 200, 1000)

        if mocDinhVi is not None:
            # Phối cảnh trực tiếp từ ảnh gốc
            daPhoiCanh_4MocDinhVi = PhoiCanh(mocDinhVi, bansao) ## lúc này ảnh đã phối chuẩn vào các mốc định vị lớn.

            daPhoiCanh_4MocDinhVi = xoayAnh_veDoc(daPhoiCanh_4MocDinhVi)

        else:
            print("LOI!! : ko tim thay vien A4 lan cac moc dinh vi")
            return img.copy()

        copy = daPhoiCanh_4MocDinhVi.copy()

        ve = Ve_Chia_anh_4_phan(daPhoiCanh_4MocDinhVi)
        #xoay anh bi lat 180 do:
        
        final_img = Xoay_Anh_ve_thang_hang(ve, 250, 500)

        output = imutils.resize(final_img, height=750)

        cv2.imshow('cuoi cung xoay ve thang hang', output)
        cv2.waitKey(0) 
        cv2.destroyAllWindows()

        return final_img
    
#hàm tăng tương phản và sắc nét cho ảnh kết quả cuối cùng---------------------------------------------------------------------------
# ĐIỀU CHỈNH ĐỘ SÁNG VÀ TƯƠNG PHẢN ---
        # Công thức: New_Pixel = alpha * Old_Pixel + beta
        # alpha = 1.5 (Tăng tương phản lên 50%)
        # beta = -30 (Giảm độ sáng đi một chút để chữ đen đậm hơn)
def DieuChinh_TuongPhan_SacNet(img):
   
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    tuongphan = cv2.convertScaleAbs(gray, alpha=1.6, beta=-150)

    
    kernel_sharpening = np.array([[0, -0.5, 0], 
                                [-0.5,  3, -0.5], 
                                [0, -0.5, 0]], dtype = np.float32)
        
    sacnet  = cv2.filter2D(tuongphan, -1, kernel_sharpening)

    anh_cuoi= imutils.resize(sacnet, height=750)
 
    cv2.imshow("Anh Sau Khi Xu Ly", anh_cuoi)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    return sacnet



# anh_cuoi = KiemTraAnh_CoVien_KoVien_vaChayChuongTrinh(img) 
# DieuChinh_TuongPhan_SacNet(anh_cuoi)

# ========================================================================================================



def XuLyAnh(img):
    # chỗ này bro gọi code chính của bro
    img = KiemTraAnh_CoVien_KoVien_vaChayChuongTrinh(img)
    img = DieuChinh_TuongPhan_SacNet(img)
    return img



# img = cv2.imread("tap2/chupdoc4_nguoc.jpg") 

# XuLyAnh(img)




# print("cv2:", cv2.__version__)
# print("numpy:", np.__version__)
# print("imutils: ",i.__version__)



# import glob #tim file
# import os #lam viec voi he dieu hanh, thao tac duong dan, thu muc, file 


# folder_path = "tap5/"      

# list_anh = glob.glob(folder_path + "*.jpg")

# for path in list_anh: 
#     print("")
#     print('////////////////////////////////////////////////////////////////////////')
#     print("Dang xu ly:", path)
#     img = cv2.imread(path)

#     ket_qua = XuLyAnh(img)

#     if ket_qua is None:
#         continue

#     # nếu muốn lưu lại
#     ten_file = os.path.basename(path)
#     cv2.imwrite("output6/" + ten_file, ket_qua) #lưu ảnh vào file 


#==============================================================================================================================================







# print(img.shape)

# cv2.imshow('casdad', img)
# cv2.waitKey(0) 
# cv2.destroyAllWindows()








# LÝ THUYẾT 

# Adaptive Threshold (Ngưỡng động)

        # Tham số quan trọng:
        # - 255: Giá trị pixel gán cho điểm sáng (màu trắng)
        # - cv2.ADAPTIVE_THRESH_GAUSSIAN_C: Phương pháp tính ngưỡng (Gaussian tốt hơn Mean cho ảnh có bóng)
        # - cv2.THRESH_BINARY: Loại ngưỡng (Đen/Trắng)


        # - 15: Block Size (Kích thước vùng lân cận để tính toán). Phải là số lẻ (vd: 11, 15, 21, 31...).
        #       Nếu ảnh lớn, hãy tăng số này lên (vd: 31 hoặc 41).


        # - 10: Constant C (Hằng số trừ đi).
        #       Giá trị này càng lớn thì nhiễu càng ít nhưng nét chữ có thể bị đứt.
        #       Giá trị càng nhỏ thì nét chữ đậm nhưng có thể còn nhiễu đen.


        # binary_img = cv2.adaptiveThreshold(blurred, 255, 
        #                                 cv2.ADAPTIVE_THRESH_GAUSSIAN_C, 
        #                                 cv2.THRESH_BINARY, 31, 15)



 

    








