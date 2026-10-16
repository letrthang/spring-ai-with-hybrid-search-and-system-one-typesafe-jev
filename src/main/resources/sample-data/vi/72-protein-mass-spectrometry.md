# Công nghệ sinh học cơ bản — khối phổ protein

## Khối phổ đo gì
Khối phổ đo khối lượng của phân tử bằng cách biến chúng thành ion và sắp xếp theo tỉ số khối lượng trên điện tích,
viết là m/z. Mỗi máy khối phổ có ba phần: nguồn ion tạo điện tích cho phân tử, bộ phân tích khối tách các ion, và đầu
dò đếm chúng. Kết quả là một phổ khối, tức đồ thị cường độ tín hiệu theo m/z.

## Dalton
Dalton (Da) là đơn vị dùng cho khối lượng phân tử trong khối phổ. Một dalton bằng một phần mười hai khối lượng của
nguyên tử carbon-12, xấp xỉ khối lượng của một proton hoặc một nguyên tử hydro. Trung bình mỗi amino acid trong
protein đóng góp khoảng 110 Da, nên một protein dài 300 amino acid nặng khoảng 33 kDa.

## Ion hóa phun điện
Ion hóa phun điện (ESI) biến protein và peptide trong dung dịch thành ion ở pha khí mà không làm vỡ chúng. Chất lỏng
được phun ra từ một kim nhỏ đặt ở điện thế vài kilovolt, tạo thành các giọt tích điện co lại khi dung môi bay hơi cho
đến khi ion được giải phóng. ESI thường gắn nhiều điện tích lên một protein lớn, tạo ra một dãy peak gọi là đường bao
trạng thái điện tích, và phần mềm chuyển dãy peak này về một khối lượng duy nhất.

## MALDI
MALDI, giải hấp ion hóa bằng laser có hỗ trợ chất nền, ion hóa phân tử bằng cách bắn laser vào mẫu được trộn với một
chất nền hấp thụ ánh sáng. Mẫu và chất nền khô lại cùng nhau thành tinh thể trên một tấm kim loại, và mỗi xung laser
nhấc lên và ion hóa một lượng nhỏ vật liệu, phần lớn mang một điện tích. MALDI nhanh và được dùng trong các phòng xét
nghiệm vi sinh lâm sàng để định danh vi khuẩn từ dấu vân tay protein của chúng chỉ trong vài phút.

## Bộ phân tích khối
Bộ phân tích khối tách các ion theo m/z của chúng. Bộ phân tích thời gian bay (TOF) đo thời gian ion bay hết một ống,
vì ion nhẹ hơn đến trước, còn bộ tứ cực dùng điện trường dao động để chỉ cho các giá trị m/z được chọn đi qua. Bộ
phân tích Orbitrap và FT-ICR đo tốc độ quay vòng của ion và đạt độ phân giải rất cao, tách được các khối lượng chỉ
chênh nhau vài phần triệu.

## Dấu vân tay khối lượng peptide
Dấu vân tay khối lượng peptide định danh một protein từ khối lượng các peptide của nó. Trước tiên protein được cắt
bằng enzyme trypsin, cắt sau các amino acid lysine và arginine, tạo ra một tập peptide có khối lượng dự đoán được.
Các khối lượng đo được được so với các peptide dự đoán từ một cơ sở dữ liệu protein, và protein khớp nhất được báo
cáo.

## Khối phổ song song
Khối phổ song song, viết là MS/MS, chọn một ion peptide, bẻ nó thành các mảnh và đo các mảnh đó. Va chạm với một khí
trơ thường làm đứt các liên kết peptide, tạo ra hai thang mảnh gọi là ion b và ion y. Hiệu khối lượng giữa các mảnh
liền kề tương ứng với từng amino acid, nên có thể đọc trình tự của peptide từ phổ.

## Proteomics từ dưới lên
Proteomics từ dưới lên định danh hàng nghìn protein trong một mẫu bằng cách cắt tất cả chúng thành peptide trước. Hỗn
hợp peptide được tách bằng sắc ký lỏng pha đảo và đưa thẳng vào máy khối phổ, một hệ thống gọi là LC-MS/MS. Phần mềm
tìm kiếm cơ sở dữ liệu ghép các phổ mảnh với các trình tự đã biết, và một lần chạy 1 đến 2 giờ có thể định danh vài
nghìn protein người.

## Định lượng protein bằng khối phổ
Khối phổ có thể đo lượng của từng protein, chứ không chỉ cho biết có những protein nào. Phương pháp không đánh dấu so
sánh tín hiệu của cùng các peptide giữa các mẫu, còn các phương pháp đánh dấu như SILAC và TMT đánh dấu mẫu bằng đồng
vị bền để có thể trộn nhiều mẫu và đo trong một lần chạy. Phương pháp nhắm đích theo dõi một danh sách peptide được
chọn, thường dùng lượng đã biết của peptide đánh dấu đồng vị nặng làm chất chuẩn nội.

## Phân tích khối lượng nguyên vẹn
Phân tích khối lượng nguyên vẹn đo toàn bộ protein mà không cắt thành peptide. Phân tích khối lượng nguyên vẹn được
dùng để xác nhận một protein tái tổ hợp có đúng trình tự mong đợi và để phát hiện các thay đổi như thiếu một amino
acid hoặc gắn thêm nhóm đường. Glycosyl hóa, phosphoryl hóa (+80 Da) và oxy hóa (+16 Da) đều làm khối lượng dịch đi
một lượng đã biết, nên độ lớn của sự dịch chuyển chỉ ra loại biến đổi.
