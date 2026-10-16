# Vật lý cơ bản — pin mặt trời và pin sạc

## Cách pin mặt trời hoạt động
Pin mặt trời biến ánh sáng trực tiếp thành điện nhờ lớp tiếp giáp giữa silic loại p và silic loại n. Khi một photon
đủ năng lượng bị hấp thụ, nó giải phóng một electron và để lại một lỗ trống, và điện trường tại tiếp giáp p-n đẩy
chúng tách ra để dòng điện chạy qua mạch ngoài. Một tế bào pin mặt trời bằng silic cho điện áp hở mạch khoảng 0.6
đến 0.7 V, dù kích thước lớn hay nhỏ.

## Hiệu suất pin mặt trời
Hiệu suất pin mặt trời là phần trăm công suất ánh sáng chiếu tới được tế bào biến thành công suất điện. Tấm pin
silic thương mại đạt khoảng 18 đến 23 %, trong khi giới hạn lý thuyết cho tế bào một tiếp giáp, gọi là giới hạn
Shockley–Queisser, vào khoảng 33 %. Các tế bào đa tiếp giáp xếp chồng nhiều vật liệu đã vượt 45 % trong phòng thí
nghiệm và được dùng trên vệ tinh.

## Đường đặc tuyến dòng–áp và điểm công suất cực đại
Đường đặc tuyến dòng–áp (I–V) của pin mặt trời cho thấy tế bào cho dòng lớn nhất khi ngắn mạch và điện áp lớn nhất
khi hở mạch, nhưng ở cả hai đầu công suất đều bằng không. Công suất P = V × I đạt đỉnh tại điểm công suất cực đại,
thường ở khoảng 80 % điện áp hở mạch. Bộ điều khiển sạc và biến tần có chức năng dò điểm công suất cực đại (MPPT)
liên tục điều chỉnh tải để luôn ở điểm này khi ánh nắng thay đổi.

## Ghép tế bào nối tiếp và song song
Các tế bào pin mặt trời được nối tiếp bên trong một tấm pin để tăng điện áp, và các tấm pin được ghép nối tiếp hoặc
song song để đạt điện áp và dòng điện mà hệ thống cần. Một tấm pin kiểu cổ điển có 60 tế bào nối tiếp và điện áp hở
mạch khoảng 37 đến 40 V. Vì các tế bào nối tiếp có chung một dòng điện, một tế bào bị che bóng có thể làm giảm sản
lượng của cả chuỗi, nên tấm pin có các điốt bypass để dẫn dòng vòng qua nhóm tế bào bị che.

## Nhiệt độ và công suất tấm pin mặt trời
Tấm pin mặt trời cho công suất thấp hơn khi nóng, vì điện áp của tế bào giảm khi nhiệt độ tăng. Một tấm pin
silic điển hình mất khoảng 0.3 đến 0.5 % công suất cho mỗi độ C trên nhiệt độ thử nghiệm 25 °C. Trên mái nhà
nắng gắt, tế bào có thể nóng tới 60 °C, làm công suất giảm khoảng 10 đến 18 %, nên để không khí lưu thông tốt
phía sau tấm pin sẽ có ích.

## Số giờ nắng đỉnh và điện năng mỗi ngày
Số giờ nắng đỉnh là số giờ mỗi ngày cần để nhận đủ tổng năng lượng ánh sáng trong ngày nếu cường độ luôn ở mức
chuẩn 1000 W/m². Một nơi nhiều nắng có thể có 5 giờ nắng đỉnh, nên một dàn pin 1 kW tạo ra khoảng 5 kWh mỗi ngày
trước tổn hao, hay khoảng 4 kWh sau tổn hao của biến tần, dây dẫn và nhiệt độ. Sản lượng hằng năm dao động từ
khoảng 900 kWh cho mỗi kW lắp đặt ở Bắc Âu nhiều mây đến hơn 1600 kWh ở những vùng nhiều nắng.

## Cách pin sạc hoạt động
Pin sạc lưu trữ năng lượng dưới dạng hóa học trong hai điện cực được ngăn cách bởi chất điện phân, cho ion đi qua
nhưng không cho electron đi qua. Trong pin lithium-ion, khi phóng điện các ion lithium di chuyển từ cực âm bằng
graphit sang cực dương bằng oxit kim loại, còn electron đi qua mạch ngoài và cấp điện cho thiết bị. Khi sạc, các
ion lithium bị đẩy ngược trở lại graphit, sẵn sàng cho lần phóng điện tiếp theo.

## Các loại hóa học pin phổ biến
Các loại pin sạc phổ biến khác nhau về điện áp mỗi cell, mật độ năng lượng và tuổi thọ. Ắc quy chì-axit cho 2.0
V mỗi cell, nên ắc quy ô tô 12 V có sáu cell, và chỉ lưu trữ khoảng 30 đến 40 Wh/kg. Cell lithium-ion dùng cực
dương oxit niken hoặc coban cho khoảng 3.6 đến 3.7 V và 150 đến 270 Wh/kg, phù hợp với điện thoại và ô tô điện,
còn cell lithium sắt phốt phát (LiFePO4) cho 3.2 V, hy sinh một phần mật độ năng lượng để đổi lấy tuổi thọ dài
hơn và an toàn hơn.

## Tốc độ C và độ sâu xả
Tốc độ C biểu thị dòng sạc hoặc xả so với dung lượng pin: với pin 100 Ah, 1C là 100 A và 0.5C là 50 A, làm cạn
pin trong khoảng 2 giờ. Độ sâu xả (DoD) là phần dung lượng đã dùng trước khi sạc lại. Xả nông giúp kéo dài tuổi
thọ rất nhiều: ắc quy chì-axit bền hơn nhiều khi xả 50 % DoD so với 100 %, và pin LiFePO4 thường đạt 3000 đến
6000 chu kỳ ở 80 % DoD.

## Tính dung lượng cho hệ điện mặt trời nhỏ có lưu trữ
Bộ pin lưu trữ cho một hệ điện mặt trời độc lập nhỏ được tính từ điện năng tiêu thụ mỗi ngày, số ngày dự phòng mong
muốn và độ sâu xả cho phép. Ví dụ, một căn nhà gỗ dùng 2 kWh mỗi ngày với 2 ngày dự phòng cần 4 kWh dung lượng sử
dụng được, tức 5 kWh pin LiFePO4 ở 80 % DoD hoặc 8 kWh ắc quy chì-axit ở 50 % DoD. Sau đó dàn pin mặt trời được
chọn đủ để nạp lại lượng điện dùng mỗi ngày trong tháng ít nắng nhất, có tính thêm khoảng 20 % tổn hao.
