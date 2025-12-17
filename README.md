# OpenMUC Project Setup Guide

Tài liệu này hướng dẫn chi tiết cách cài đặt và chạy dự án OpenMUC sau khi pull code về máy.

## 1. Yêu cầu hệ thống (Prerequisites)
- **Java Development Kit (JDK)**: Version 21 (Bắt buộc).
- **Gradle**: Version 8.5 (Đã được cấu hình sẵn trong Wrapper).
- **Git**: Để quản lý source code.

> **Lưu ý về Gradle:**
> Dự án đã được cập nhật lên Gradle 8.5 để tương thích với Java 21.
> Khi bạn chạy lệnh `./gradlew` lần đầu tiên sau khi pull code, nó sẽ **tự động tải** Gradle 8.5 về máy. Bạn không cần cài đặt Gradle thủ công.

## 2. Các bước cài đặt (Installation Steps)

### Bước 1: Pull Code
Lấy code mới nhất từ repository:
```bash
git pull origin main
```

### Bước 2: Cài đặt OSGi Framework
Dự án OpenMUC cần một OSGi framework (Apache Felix) để chạy. Các file này không được commit lên git (nằm trong `.gitignore`).
Chúng ta sẽ dùng script có sẵn để tự động tải và cài đặt:

**Chạy lệnh sau tại thư mục gốc của dự án:**
```bash
chmod +x setup_openmuc.sh  # Cấp quyền thực thi (nếu cần)
./setup_openmuc.sh
```
*Script này sẽ tự động tải OpenMUC 0.20.1, giải nén và copy các thư mục `bin` và `felix` vào thư mục `framework/` của dự án.*

### Bước 3: Build và Deploy Bundles
Sau khi có framework, chúng ta cần build các module (như modbus driver) và copy chúng vào thư mục `framework/bundle`.

**Chạy lệnh Gradle:**
```bash
./gradlew updateBundles -x test
./gradlew :{project mói tạo hoặc thay đổi}:clean :{project mói tạo hoặc thay đổi}:build
rm -rf ./framework/bundle/{project mới tạo hoặc thay đổi}.jar cp ./build/libs-all/{project mói tạo hoặc thay đổi} ./framework/bundle
```
*Lệnh này sẽ:*
1.  Clean và Build tất cả các subprojects.
2.  Copy các file JAR (bao gồm cả `jSerialComm` và `openmuc-driver-modbus`) vào thư mục `framework/bundle`.
3.  `-x test`: Bỏ qua chạy test để build nhanh hơn (tùy chọn).

## 3. Chạy ứng dụng (Running OpenMUC)

Sau khi build thành công, bạn có thể khởi động OpenMUC:

```bash
cd framework
./bin/openmuc start -fg
```
- `start`: Lệnh khởi động.
- `-fg`: Chạy ở chế độ foreground (để thấy log trực tiếp trên màn hình).

## 4. Troubleshooting (Xử lý lỗi thường gặp)

### Lỗi `NoClassDefFoundError: com/fazecast/jSerialComm/...`
Nếu bạn gặp lỗi này khi start, nghĩa là bundle Modbus chưa nhận được thư viện `jSerialComm`.

**Cách khắc phục:**
1.  Đảm bảo bạn đã chạy `./gradlew updateBundles`.
2.  Kiểm tra thư mục `framework/bundle` xem có file `jSerialComm-2.10.4.jar` chưa.
3.  Xóa cache của Felix và start lại:
    ```bash
    rm -rf framework/felix-cache/*
    cd framework && ./bin/openmuc start -fg
    ```

### Lỗi `Unsupported class file major version 65`
Lỗi này do bạn đang dùng Java cũ hơn Java 21 để chạy Gradle 8.5. Hãy đảm bảo `JAVA_HOME` trỏ đến JDK 21.

## 5. Hướng dẫn cập nhật Gradle (Dành cho Developer)

Dự án ban đầu sử dụng Gradle 7.6.2, nhưng đã được nâng cấp lên **Gradle 8.5** để hỗ trợ Java 21.

Nếu bạn cần cập nhật phiên bản Gradle trong tương lai, hãy chạy lệnh sau:

```bash
./gradlew wrapper --gradle-version <version_mong_muốn>
```

Ví dụ (lệnh đã dùng để nâng cấp lên 8.5):
```bash
./gradlew wrapper --gradle-version 8.5
```
Sau đó commit các file `gradle/wrapper/gradle-wrapper.properties` và `gradle/wrapper/gradle-wrapper.jar` lên git.

## 6. Hướng dẫn Debug trong IntelliJ (Chi tiết)

Để debug code (ví dụ đặt breakpoint trong Modbus Driver), bạn cần chạy OpenMUC với debug agent và kết nối từ IntelliJ.

### Bước 1: Tạo Remote Debug Configuration trong IntelliJ

1.  Mở menu **Run** → **Edit Configurations...**
2.  Nhấn dấu **+** (góc trên bên trái) và chọn **Remote JVM Debug**.
3.  Đặt tên cho configuration: `OpenMUC Remote Debug`.
4.  Cấu hình như sau:
    *   **Debugger mode**: `Attach to remote JVM`
    *   **Host**: `localhost`
    *   **Port**: `5005`
    *   **Command line arguments for remote JVM**: (để mặc định, đã có sẵn)
    *   **Use module classpath**: Chọn `openmuc-driver-modbus` (hoặc module bạn muốn debug)
5.  Nhấn **Apply** → **OK**.

### Bước 2: Chạy OpenMUC ở chế độ Debug

**Lưu ý quan trọng:** Bạn phải chạy OpenMUC với debug agent **trước** khi kết nối từ IntelliJ.

#### Cách 1: Dùng script có sẵn (Đơn giản nhất)
Chạy lệnh sau trong Terminal:
```bash
./debug_openmuc.sh
```
**Kiểm tra:** OpenMUC phải đang chạy và bạn thấy log hiện ra liên tục. **Giữ nguyên Terminal này** (đừng tắt).

#### Cách 2: Chạy thủ công
Nếu script không hoạt động, chạy trực tiếp:
```bash
cd framework
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar felix/felix.jar
```

### Bước 3: Kết nối Debugger từ IntelliJ

1.  Sau khi OpenMUC đang chạy (Bước 2), quay lại IntelliJ.
2.  Chọn configuration `OpenMUC Remote Debug` trên thanh công cụ (dropdown bên cạnh nút Run/Debug).
3.  Nhấn nút **Debug** (biểu tượng con bọ 🐞).
4.  Nếu kết nối thành công, Console của IntelliJ sẽ hiện:
    ```
    Connected to the target VM, address: 'localhost:5005', transport: 'socket'
    ```

### Bước 4: Đặt Breakpoint và Debug

1.  **Mở file code** bạn muốn debug (ví dụ: `projects/driver/modbus/src/main/java/org/openmuc/framework/driver/modbus/ModbusDriver.java`).
2.  **Đặt breakpoint**: Click vào **lề trái** (bên cạnh số dòng) → Xuất hiện chấm đỏ 🔴.
3.  **Kích hoạt code**: Thực hiện hành động để code chạy đến breakpoint (ví dụ: gọi REST API để kết nối Modbus).
4.  **Khi code dừng tại breakpoint**:
    *   Tab **Debugger** sẽ tự động mở.
    *   **Variables**: Xem giá trị các biến.
    *   **Call Stack**: Xem chuỗi hàm gọi.
    *   **Controls**:
        - **Step Over (F8)**: Chạy qua dòng hiện tại.
        - **Step Into (F7)**: Nhảy vào hàm được gọi.
        - **Step Out (Shift+F8)**: Thoát khỏi hàm hiện tại.
        - **Resume (F9)**: Tiếp tục chạy đến breakpoint tiếp theo.

### Xử lý lỗi thường gặp

#### Lỗi: `Unable to open debugger port: Connection refused`

**Nguyên nhân:** OpenMUC chưa chạy hoặc không mở cổng 5005.

**Cách khắc phục:**
1.  Kiểm tra xem OpenMUC có đang chạy không (trong Terminal).
2.  Kiểm tra cổng 5005 đã mở chưa:
    ```bash
    lsof -i :5005
    ```
    Nếu không có kết quả → OpenMUC chưa chạy ở chế độ debug.
3.  Chạy lại lệnh `./debug_openmuc.sh` và đợi cho đến khi thấy log hiện ra.
4.  Sau đó mới nhấn **Debug** trong IntelliJ.

#### Lỗi: Breakpoint không dừng lại

**Nguyên nhân:**
*   Code chưa được thực thi (chưa có request/action kích hoạt).
*   Source code không khớp với JAR đang chạy.

**Cách khắc phục:**
1.  Đảm bảo bạn đã rebuild code mới nhất:
    ```bash
    ./gradlew :openmuc-driver-modbus:build updateBundles -x test
    ```
2.  Xóa cache Felix và restart:
    ```bash
    rm -rf framework/felix-cache/*
    # Sau đó chạy lại ./debug_openmuc.sh
    ```
3.  Kích hoạt hành động để code chạy đến breakpoint (ví dụ: gọi REST API).

#### Lỗi: Debugger ngắt kết nối ngay sau khi kết nối

**Nguyên nhân:** OpenMUC bị crash hoặc tắt.

**Cách khắc phục:**
1.  Kiểm tra log trong Terminal xem có lỗi gì không.
2.  Đảm bảo tất cả dependencies đã được build đúng (đặc biệt là `jSerialComm`).

### Lưu ý
*   **Port 5005** chỉ là cổng kết nối giữa IntelliJ và Java process, không ảnh hưởng logic ứng dụng.
*   Bạn có thể thay đổi port bằng cách sửa tham số `-agentlib:jdwp=...address=*:PORT` trong script.
*   Khi debug xong, nhấn **Stop** (hình vuông đỏ) trong IntelliJ và `Ctrl+C` trong Terminal để tắt OpenMUC.

hello, tat cat moi ng bat dau dung git nhe
