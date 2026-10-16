# Công nghệ sinh học cơ bản — qPCR và RT-PCR

## qPCR đo cái gì
PCR định lượng (qPCR) đo lượng một trình tự DNA cụ thể có trong mẫu bằng cách theo dõi quá trình khuếch đại PCR
ngay khi nó diễn ra. Tín hiệu huỳnh quang được đọc sau mỗi chu kỳ, nên máy ghi lại một đường cong biểu diễn lượng
DNA theo số chu kỳ. qPCR còn được gọi là PCR thời gian thực và được dùng để nghiên cứu biểu hiện gen, phát hiện
virus và kiểm tra vật liệu biến đổi gen trong thực phẩm.

## RT-PCR và RT-qPCR
RT-PCR, hay PCR phiên mã ngược, là PCR bắt đầu từ RNA thay vì DNA. Trước tiên, enzyme phiên mã ngược sao chép RNA
thành DNA bổ sung (cDNA), sau đó PCR thông thường khuếch đại cDNA. Khi bước PCR được chạy dưới dạng PCR thời gian
thực, phương pháp được gọi là RT-qPCR, là xét nghiệm tiêu chuẩn cho các virus RNA như SARS-CoV-2 và để đo mức mRNA;
chữ "RT" nghĩa là phiên mã ngược, không phải thời gian thực.

## Mồi cho phản ứng phiên mã ngược
Phiên mã ngược trong RT-PCR có thể được mồi theo ba cách phổ biến. Mồi oligo(dT) gắn vào đuôi poly(A) và chỉ sao
chép mRNA, mồi ngẫu nhiên gồm sáu base (random hexamer) gắn dọc khắp mọi RNA, còn mồi đặc hiệu gen chỉ sao chép một
đích. Bộ kit một bước chạy phiên mã ngược và PCR trong cùng một ống, còn phương pháp hai bước tạo ra một tập hợp
cDNA có thể dùng để kiểm tra nhiều gen.

## Phát hiện bằng SYBR Green
SYBR Green là thuốc nhuộm huỳnh quang gắn vào bất kỳ DNA mạch kép nào và phát sáng mạnh hơn khoảng 1.000 lần khi đã
gắn. Khi sản phẩm PCR tích lũy, tín hiệu SYBR Green tăng theo. SYBR Green rẻ và dùng được với mọi cặp mồi, nhưng vì
nó cũng phát hiện cả dimer mồi và sản phẩm sai, cần chạy đường cong nóng chảy để kiểm tra rằng chỉ có một sản phẩm
được tạo ra.

## Mẫu dò thủy phân
Mẫu dò thủy phân, thường gọi là mẫu dò TaqMan, là một đoạn DNA ngắn có gắn nhãn, bám vào bên trong trình tự đích
giữa hai mồi. Mẫu dò mang một thuốc nhuộm báo cáo ở một đầu và một chất dập tắt ở đầu kia, nên không phát sáng khi
còn nguyên vẹn; khi Taq polymerase kéo dài qua mẫu dò, hoạt tính exonuclease 5′ → 3′ của enzyme cắt mẫu dò và giải
phóng thuốc nhuộm báo cáo để phát sáng. Mẫu dò đặc hiệu hơn SYBR Green và, với các màu thuốc nhuộm khác nhau, cho
phép đo nhiều đích trong cùng một ống.

## Đường cong khuếch đại và ngưỡng
Đường cong khuếch đại qPCR có ba pha: đường nền phẳng, pha lũy thừa và pha bình nguyên. Trong pha lũy thừa, sản
phẩm tăng gần gấp đôi sau mỗi chu kỳ, và chỉ pha này được dùng để định lượng. Ngưỡng là một mức huỳnh quang được
đặt cao hơn nhiễu nền và nằm trong pha lũy thừa, và chu kỳ mà đường cong cắt ngưỡng là giá trị Cq, còn gọi là giá
trị Ct.

## Đường chuẩn và hiệu suất
Đường chuẩn qPCR được tạo bằng cách chạy một dãy pha loãng 10 lần của khuôn đã biết lượng và vẽ Ct theo logarit của
lượng ban đầu. Ở hiệu suất 100%, độ dốc là −3,32, nghĩa là Ct tăng khoảng 3,3 chu kỳ cho mỗi lần pha loãng 10 lần.
Các xét nghiệm tốt thường có hiệu suất 90–110%, tương ứng với độ dốc khoảng −3,6 đến −3,1, và R² lớn hơn 0,98.

## Định lượng tương đối bằng ΔΔCt
Phương pháp ΔΔCt so sánh mức biểu hiện của một gen trong mẫu được xử lý với mẫu đối chứng mà không cần đường chuẩn.
Trước tiên, Ct của mỗi gen đích được chuẩn hóa theo một gen tham chiếu như GAPDH để có ΔCt, sau đó ΔCt của mẫu xử
lý được so với ΔCt của mẫu đối chứng để có ΔΔCt, và mức thay đổi bằng 2^(−ΔΔCt). Ví dụ, ΔΔCt bằng −2 nghĩa là tăng
gấp bốn lần; phương pháp ΔΔCt giả định cả hai gen được khuếch đại với hiệu suất gần 100%.

## Đường cong nóng chảy
Đường cong nóng chảy dùng để kiểm tra sản phẩm của một lần chạy qPCR với SYBR Green. Sau chu kỳ cuối, máy làm nóng
mẫu từ từ từ khoảng 60 °C lên 95 °C và đọc huỳnh quang, tín hiệu này giảm khi DNA mạch kép tách ra. Một đỉnh nhọn
duy nhất trên đường cong nóng chảy nghĩa là có một sản phẩm đặc hiệu, còn các đỉnh phụ ở nhiệt độ thấp hơn thường
cho thấy dimer mồi hoặc khuếch đại không đặc hiệu.

## Đối chứng trong qPCR
Các mẫu đối chứng cho biết kết quả qPCR có đáng tin hay không. Đối chứng không khuôn (NTC), chứa nước thay cho mẫu,
không được cho tín hiệu và giúp phát hiện nhiễm bẩn, còn đối chứng không RT, tức mẫu RNA được xử lý mà không có
enzyme phiên mã ngược, giúp phát hiện DNA bộ gen lẫn vào. Chạy mỗi mẫu lặp lại ba lần kỹ thuật và dùng nhiều mẫu
lặp sinh học giúp phân biệt khác biệt thật với sai số do pipet.
