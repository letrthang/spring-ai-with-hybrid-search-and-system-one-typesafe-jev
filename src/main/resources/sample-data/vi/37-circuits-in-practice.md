# Vật lý cơ bản — mạch điện trong thực tế

## Dòng điện xoay chiều và dòng điện một chiều
Dòng điện một chiều (DC) chỉ chạy theo một chiều, như dòng điện từ pin hay tấm pin mặt trời. Dòng điện xoay chiều (AC)
đổi chiều nhiều lần mỗi giây: điện lưới có tần số 50 Hz ở châu Âu và phần lớn châu Á, và 60 Hz ở Bắc Mỹ. Lưới điện
dùng dòng xoay chiều vì máy biến áp có thể dễ dàng thay đổi hiệu điện thế của nó.

## Điện áp lưới và giá trị hiệu dụng
Điện áp lưới được ghi theo giá trị hiệu dụng (RMS), tức là hiệu điện thế một chiều không đổi cho cùng công suất tỏa
nhiệt. Điện lưới 230 V ở châu Âu thực ra có giá trị cực đại khoảng 325 V, vì đỉnh của sóng hình sin bằng √2 lần giá trị
hiệu dụng. Điện lưới ở Mỹ là 120 V hiệu dụng và có giá trị cực đại khoảng 170 V.

## Cầu chì và aptomat
Cầu chì là một sợi dây mảnh sẽ nóng chảy và ngắt mạch nếu dòng điện quá lớn, bảo vệ dây dẫn khỏi quá nhiệt và cháy.
Aptomat (cầu dao tự động) làm cùng nhiệm vụ đó bằng một công tắc tự nhảy và có thể bật lại. Định mức của cầu chì nên
chỉ cao hơn một chút so với dòng điện bình thường, ví dụ 13 A cho lò sưởi 2500 W dùng điện 230 V.

## Nối đất
Dây nối đất nối vỏ kim loại của thiết bị điện với mặt đất. Nếu dây pha bị lỏng và chạm vào vỏ, một dòng điện lớn sẽ
chạy thẳng xuống đất và làm đứt cầu chì hoặc nhảy aptomat, thay vì chạy qua người chạm vào. Các thiết bị có vỏ nhựa có
thể được cách điện kép và không cần dây nối đất.

## Thiết bị chống dòng rò
Thiết bị chống dòng rò (RCD) so sánh dòng điện trong dây pha và dây trung tính và ngắt điện trong khoảng 30 mili giây
nếu hai dòng này khác nhau. Chênh lệch chỉ 30 mA nghĩa là dòng điện đang rò ra đâu đó, có thể là qua cơ thể người. RCD
bảo vệ chống điện giật ở phòng tắm, nhà bếp và các ổ cắm ngoài trời.

## Đoản mạch
Đoản mạch (chập mạch) là một đường dẫn ngoài ý muốn có điện trở rất nhỏ, chẳng hạn hai dây trần chạm nhau, khiến một
dòng điện rất lớn chạy qua. Vì hầu như không có gì hạn chế dòng điện, dây dẫn nóng lên rất nhanh và có thể chảy hoặc
gây cháy. Đặt một chiếc cờ lê chạm ngang hai cực ắc quy ô tô có thể làm hàng trăm ampe chạy qua nó.

## Pin và điện trở trong
Pin cung cấp một suất điện động (ε), nhưng một phần hiệu điện thế bị mất bên trong pin do điện trở trong của nó. Hiệu
điện thế giữa hai cực là V = ε − I r, nên nó giảm khi lấy ra dòng điện lớn hơn. Một pin 1.5 V có điện trở trong 0.5 Ω
chỉ cho ra 1.0 V khi cung cấp dòng 1 A.

## Dung lượng pin
Dung lượng pin được đo bằng ampe-giờ (Ah) hoặc miliampe-giờ (mAh): dòng điện pin cung cấp được nhân với số giờ pin dùng
được. Một pin điện thoại 3000 mAh về lý thuyết có thể cung cấp dòng 300 mA trong 10 giờ. Nhân với hiệu điện thế sẽ được
năng lượng tích trữ, nên 3 Ah ở 3.7 V là khoảng 11 oát-giờ.

## Tụ điện
Tụ điện tích trữ điện tích trên hai bản dẫn điện được ngăn cách bởi một lớp cách điện. Điện dung của nó, C = Q / V, đo
bằng fara; một tụ 100 µF được tích điện đến 10 V chứa điện tích 1 mC. Tụ điện giải phóng năng lượng rất nhanh, làm phẳng
các gợn điện áp trong bộ nguồn và tạo thời gian trễ trong các mạch điện.

## Mạch chia áp
Mạch chia áp gồm hai điện trở mắc nối tiếp, chia một hiệu điện thế đầu vào thành một hiệu điện thế đầu ra nhỏ hơn. Hiệu
điện thế đầu ra trên điện trở thứ hai là V_ra = V_vào × R₂ / (R₁ + R₂). Với 12 V đặt vào điện trở 10 kΩ và 2 kΩ mắc nối
tiếp, điện trở 2 kΩ có hiệu điện thế 2 V. Nhiều mạch cảm biến được mắc theo cách này.
