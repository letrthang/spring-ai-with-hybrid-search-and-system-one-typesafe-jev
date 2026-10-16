# Vật lý cơ bản — chất bán dẫn và cảm biến

## Chất bán dẫn là gì
Chất bán dẫn là vật liệu có độ dẫn điện nằm giữa chất dẫn điện và chất cách điện. Silic là chất bán dẫn phổ biến nhất:
silic tinh khiết dẫn điện kém, nhưng độ dẫn điện của nó tăng mạnh khi nhiệt độ tăng, khi được chiếu sáng hoặc khi pha
thêm tạp chất. Khả năng điều khiển được như vậy khiến chất bán dẫn trở thành nền tảng của điốt, tranzito, pin mặt trời
và chip máy tính.

## Pha tạp: bán dẫn loại n và loại p
Pha tạp là việc thêm một lượng rất nhỏ nguyên tử tạp chất vào chất bán dẫn để thay đổi cách nó dẫn điện. Thêm phốtpho
vào silic tạo ra thêm electron tự do, cho bán dẫn loại n; thêm bo tạo ra các "lỗ trống" đóng vai trò điện tích dương,
cho bán dẫn loại p. Chỉ cần một nguyên tử tạp chất trên một triệu nguyên tử silic cũng làm độ dẫn điện thay đổi rất lớn.

## Điốt
Điốt chỉ cho dòng điện chạy dễ dàng theo một chiều, từ phía p sang phía n của lớp chuyển tiếp p–n. Điốt silic cần hiệu
điện thế khoảng 0.7 V đặt vào mới dẫn điện theo chiều thuận, và nó chặn dòng điện theo chiều ngược. Điốt biến dòng xoay
chiều thành dòng một chiều trong bộ nguồn, quá trình này gọi là chỉnh lưu.

## Điốt phát quang
Điốt phát quang (LED) phát ra ánh sáng khi có dòng điện chạy qua, vì các electron rơi vào lỗ trống và giải phóng năng
lượng dưới dạng phôtôn. Màu ánh sáng phụ thuộc vào vật liệu bán dẫn: LED đỏ cần khoảng 2 V còn LED xanh lam khoảng 3 V.
LED phải được mắc nối tiếp với một điện trở để hạn chế dòng điện, thường ở khoảng 20 mA.

## Tranzito
Tranzito là một linh kiện bán dẫn dùng làm công tắc hoặc khuếch đại, trong đó một tín hiệu nhỏ ở một cực điều khiển một
dòng điện lớn hơn nhiều chạy giữa hai cực còn lại. Trong MOSFET, hiệu điện thế đặt vào cực cổng (gate) đóng ngắt dòng
điện giữa cực nguồn (source) và cực máng (drain). Một bộ vi xử lý máy tính hiện đại chứa hàng tỉ tranzito bật tắt hàng
tỉ lần mỗi giây.

## Nhiệt điện trở
Nhiệt điện trở (thermistor) là điện trở có giá trị thay đổi mạnh theo nhiệt độ. Ở loại NTC thông dụng, điện trở giảm
khi nhiệt độ tăng: một nhiệt điện trở điển hình 10 kΩ ở 25 °C giảm còn khoảng 3 kΩ ở 60 °C. Nhiệt điện trở dùng để đo
nhiệt độ trong nhiệt kế điện tử, tủ lạnh, bộ sạc pin và động cơ ô tô.

## Quang điện trở
Quang điện trở (LDR) có điện trở giảm khi ánh sáng chiếu vào nó mạnh hơn. Trong bóng tối, quang điện trở có thể có điện
trở khoảng 1 MΩ, giảm xuống còn vài trăm ôm khi có ánh sáng mạnh. Khi mắc vào mạch chia áp, nó cho hiệu điện thế đầu ra
thay đổi theo mức độ sáng, được dùng để tự bật đèn đường lúc chạng vạng.

## Điốt quang
Điốt quang (photodiode) là điốt tạo ra dòng điện khi ánh sáng chiếu vào lớp chuyển tiếp p–n của nó, vì phôtôn giải
phóng các electron và lỗ trống. Dòng điện tỉ lệ với cường độ ánh sáng và phản ứng trong vòng vài nano giây. Điốt quang
dùng để đọc đĩa quang, nhận tín hiệu từ cáp quang, và đo ánh sáng trong máy ảnh và máy đo nồng độ oxy trong máu.

## Cảm biến biến dạng
Cảm biến biến dạng (strain gauge) là một lá kim loại mỏng hình zíc zắc được dán lên bề mặt, có điện trở tăng nhẹ khi bị
kéo giãn. Sự thay đổi này rất nhỏ, khoảng 0.2% với biến dạng 0.1%, nên được đo bằng mạch cầu. Cảm biến biến dạng là bộ
phận cảm nhận bên trong cân nhà bếp điện tử, cân sức khỏe và cảm biến lực (load cell) công nghiệp.

## Chuyển đổi tương tự – số
Bộ chuyển đổi tương tự – số (ADC) biến hiệu điện thế liên tục của cảm biến thành một con số mà máy tính có thể xử lý.
Một ADC 10 bit chia dải đầu vào thành 2¹⁰ = 1024 bậc, nên với dải 5 V mỗi bậc khoảng 4.9 mV. Càng nhiều bit thì độ phân
giải càng mịn: ADC 12 bit có 4096 bậc.
