# 智能COB灯带控制Android应用

这是一个基于Android的智能COB灯带控制应用，通过MQTT协议远程控制双模COB灯带（冷灯和暖灯）。

## 功能特性

### 🎨 美观的暗色主题界面
- 现代化的Material Design暗色主题
- 直观的用户界面设计
- 响应式布局适配不同屏幕尺寸

### 🌈 五颜六色环形色盘
- 位于界面中央的装饰性色盘
- 支持多种颜色选择
- 虽然不直接影响设备，但提供视觉反馈

### 🔘 中央开关控制
- 位于色盘中央的电源开关按钮
- 一键控制所有灯带的开关状态
- 实时状态反馈

### 🎚️ 双触点亮度滑块
- **冷灯控制**：ID=1的灯带亮度范围控制
- **暖灯控制**：ID=2的灯带亮度范围控制
- 支持0-100%的亮度范围调节
- 双触点设计，可同时设置最小和最大亮度

### ⚙️ 高级设置面板
- **速度控制**：0-5.0的动画速度调节
- **相位控制**：0-10.0的相位偏移设置
- **类型选择**：支持wave、breath、fire、fade四种灯光效果
- 点击"高级设置"按钮展开/收起面板

### 📡 MQTT连接管理
- 自动连接MQTT服务器
- 实时连接状态显示
- 支持自定义MQTT服务器地址
- 长按连接状态文本可打开设置

### 💾 配置数据持久化
- 自动保存所有用户设置
- 应用重启后恢复上次配置
- 支持冷灯、暖灯、速度、相位等参数保存

## 技术架构

### 核心组件
- **MainActivity**: 主界面控制器
- **ColorPickerView**: 自定义色盘组件
- **DualRangeSlider**: 双触点滑块组件
- **MqttClient**: MQTT客户端管理
- **PreferencesManager**: 数据持久化管理
- **LightDevice**: 设备数据模型
- **SettingsDialog**: 设置对话框

### 依赖库
- **Eclipse Paho MQTT**: MQTT客户端
- **Gson**: JSON数据处理
- **Material Components**: UI组件库
- **AndroidX**: 现代Android开发库

## 安装和使用

### 1. 环境要求
- Android Studio 4.0+
- Android SDK API 29+
- 支持Android 10及以上版本

### 2. 编译安装
```bash
# 克隆项目
git clone <repository-url>

# 使用Android Studio打开项目
# 等待Gradle同步完成

# 连接Android设备或启动模拟器
# 点击运行按钮编译安装
```

### 3. MQTT服务器配置
1. 启动应用后，长按底部的连接状态文本
2. 在弹出的设置对话框中输入MQTT服务器地址
3. 格式：`tcp://IP地址:端口号`
4. 例如：`tcp://192.168.1.100:1883`
5. 点击保存并重启应用

### 4. 使用说明
1. **开关控制**：点击色盘中央的开关按钮控制灯带开关
2. **亮度调节**：拖动冷灯/暖灯区域的滑块调节亮度范围
3. **高级设置**：点击"高级设置"按钮展开更多选项
4. **颜色选择**：点击色盘不同区域选择颜色（装饰用）
5. **参数调节**：在高级设置中调节速度、相位和类型

## MQTT协议格式

应用发送的MQTT消息格式完全符合你的ESP32项目要求：

```json
{
  "freelog": false,
  "device": [
    {
      "id": 1,
      "status": true,
      "type": "wave",
      "minduty": 20,
      "maxduty": 80,
      "speed": 1.0,
      "phase": 0.0
    },
    {
      "id": 2,
      "status": true,
      "type": "wave",
      "minduty": 20,
      "maxduty": 80,
      "speed": 1.0,
      "phase": 0.0
    }
  ]
}
```

### MQTT主题
- **控制主题**: `/ll/washroom/light/light001/down/control`

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/smartcoblight/
│   │   ├── MainActivity.java          # 主界面
│   │   ├── ColorPickerView.java       # 色盘组件
│   │   ├── DualRangeSlider.java       # 双触点滑块
│   │   ├── MqttClient.java            # MQTT客户端
│   │   ├── PreferencesManager.java    # 数据持久化
│   │   ├── LightDevice.java           # 设备模型
│   │   ├── LightControlRequest.java  # 控制请求模型
│   │   └── SettingsDialog.java       # 设置对话框
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml       # 主界面布局
│   │   │   └── dialog_settings.xml    # 设置对话框布局
│   │   ├── values/
│   │   │   ├── colors.xml             # 颜色定义
│   │   │   └── themes.xml             # 主题样式
│   │   └── drawable/                  # 背景和图标资源
│   └── AndroidManifest.xml            # 应用清单
└── build.gradle.kts                   # 构建配置
```

## 自定义和扩展

### 修改MQTT主题
在`MqttClient.java`中修改`TOPIC_CONTROL`常量：
```java
private static final String TOPIC_CONTROL = "/your/custom/topic";
```

### 添加新的灯光类型
在`MainActivity.java`中修改`lightTypes`数组：
```java
private String[] lightTypes = {"wave", "breath", "fire", "fade", "your_new_type"};
```

### 调整UI颜色
在`colors.xml`中修改颜色定义：
```xml
<color name="primary_color">#FF6366F1</color>
<color name="cold_light">#FF87CEEB</color>
<color name="warm_light">#FFFFB347</color>
```

## 故障排除

### 连接问题
1. 检查MQTT服务器地址是否正确
2. 确认网络连接正常
3. 检查MQTT服务器是否运行
4. 验证端口号是否正确

### 控制无效
1. 确认MQTT连接状态为"已连接"
2. 检查ESP32设备是否在线
3. 验证MQTT主题是否正确
4. 查看ESP32串口输出确认收到消息

### 应用崩溃
1. 检查Android版本兼容性
2. 确认所有权限已授予
3. 查看Logcat输出获取详细错误信息

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交GitHub Issue
- 发送邮件至开发者

---

**注意**: 请确保你的ESP32设备已正确配置MQTT连接，并且MQTT服务器地址设置正确。首次使用建议在局域网环境下测试。
