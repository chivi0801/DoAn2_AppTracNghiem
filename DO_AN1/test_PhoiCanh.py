import cv2
import numpy as np
import matplotlib.pyplot as plt

# 1. Đọc ảnh
img = cv2.imread('tap5/chup1.jpg')
rows, cols, ch = img.shape

# 2. Chọn 4 điểm trên ảnh gốc (Source)
# Giả sử đây là toạ độ 4 góc của tờ giấy bị nghiêng
pts1 = np.float32([[22, 39], [1235, 66],  [22, 911],[1230, 884]])

# 3. Chọn 4 điểm đích mong muốn (Destination)
# Chúng ta muốn đưa nó về một hình vuông 300x300
pts2 = np.float32([[0, 0], [300, 0], [0, 300], [300, 300]])

# 4. Tính ma trận Homography (M)
M = cv2.getPerspectiveTransform(pts1, pts2)

# 5. Thực hiện biến đổi (Warp)
dst = cv2.warpPerspective(img, M, (300, 300))

# Hiển thị kết quả
plt.subplot(121), plt.imshow(img), plt.title('Input')
plt.subplot(122), plt.imshow(dst), plt.title('Output')
plt.show()