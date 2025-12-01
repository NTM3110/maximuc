# VPN/4G Connectivity Manager - Quick Start Guide

## Overview

Connectivity Manager module cho phép quản lý VPN và 4G qua REST API, tích hợp trực tiếp vào OpenMUC.

## Installation

### 1. Build Module
```bash
cd /Users/thainguyen/IntellJProjects/openmuc-at
./gradlew :openmuc-server-connectivity-manager:build
```

### 2. Deploy to OpenMUC
Module JAR tự động được copy vào `build/libs-all/`. Để deploy:
```bash
cp build/libs-all/openmuc-server-connectivity-manager-0.20.0.jar framework/bundle/
```

### 3. Chuẩn bị hệ thống

**Cài đặt strongSwan:**
```bash
sudo apt install strongswan-swanctl
```

**Tạo directories:**
```bash
sudo mkdir -p /etc/swanctl/conf.d
sudo mkdir -p /etc/swanctl/secrets.d
sudo chmod 755 /etc/swanctl/conf.d
sudo chmod 700 /etc/swanctl/secrets.d
```

**Cấu hình sudo (tạo `/etc/sudoers.d/swanctl`):**
```
yourusername ALL=(ALL) NOPASSWD: /usr/sbin/swanctl
yourusername ALL=(ALL) NOPASSWD: /usr/bin/swanctl
```

### 4. Start OpenMUC
```bash
cd framework
./bin/openmuc start -fg
```

## API Usage Examples

### Tạo VPN Connection
```bash
curl -X POST http://localhost:8888/api/vpn/connections \
  -H "Content-Type: application/json" \
  -d '{
    "name": "office-vpn",
    "category": "site-to-site",
    "auth_method": "ikev2-psk",
    "remote_address": "vpn.example.com",
    "local_identity": "local.company.com",
    "remote_identity": "remote.company.com",
    "pre_shared_key": "your-secret-key",
    "local_traffic_selector": "192.168.1.0/24",
    "remote_traffic_selector": "10.0.0.0/24",
    "andUpdate": true
  }'
```

### Liệt kê Connections
```bash
curl http://localhost:8888/api/vpn/connections
```

### Start Connection
```bash
curl -X POST http://localhost:8888/api/vpn/connections/office-vpn/start
```

### Stop Connection
```bash
curl -X POST http://localhost:8888/api/vpn/connections/office-vpn/stop
```

### Xóa Connection
```bash
curl -X DELETE http://localhost:8888/api/vpn/connections/office-vpn
```

## Troubleshooting

### Bundle không start
Kiểm tra log:
```bash
tail -f framework/log/openmuc.log
```

### Permission denied khi chạy swanctl
Kiểm tra sudo config:
```bash
sudo -l | grep swanctl
```

### API trả về 404
Kiểm tra bundle đã active:
```bash
# Trong Felix console (nếu có)
lb | grep connectivity
```

## File Locations

- **Module JAR**: `build/libs-all/openmuc-server-connectivity-manager-0.20.0.jar`
- **VPN Configs**: `/etc/swanctl/conf.d/*.conf`
- **VPN Secrets**: `/etc/swanctl/secrets.d/*.secrets.conf`
- **Source Code**: `projects/server/connectivity-manager/`

## Next Steps

Bạn có thể tích hợp UI từ openems-pro-ui để tạo giao diện quản lý VPN trực quan hơn!
