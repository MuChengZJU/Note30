# 开发指南

本文档介绍了如何在Android Studio中打开和开发Note30项目，以及如何进行测试和构建。

## 在Android Studio中打开项目

1. 启动Android Studio
2. 选择"Open an existing Android Studio project"选项
3. 在文件选择对话框中，导航到Note30项目的根目录
4. 选择项目根目录（包含`build.gradle.kts`和`settings.gradle.kts`文件的目录），然后点击"Open"
5. 等待Android Studio完成项目同步过程，这可能需要几分钟时间来下载Gradle和项目依赖

## 解决常见的构建问题

### Java版本兼容性问题

如果遇到类似以下错误：
```
Your build is currently configured to use incompatible Java X.X.X and Gradle X.X
```

这是因为使用的Java版本与Gradle版本不兼容。项目已配置为使用Gradle 8.5，它支持Java 19及以下版本。

解决方法：
1. 在Android Studio中，选择"File" > "Project Structure"
2. 在"SDK Location"选项卡中，检查"Gradle Settings"
3. 确保"Gradle JDK"设置为Java 17或更低版本
4. 点击"OK"并重新同步项目

### 清理Gradle缓存

如果项目同步失败，可以尝试清理Gradle缓存：

1. 关闭Android Studio
2. 删除项目目录下的`.gradle`文件夹：`rm -rf .gradle`
3. 删除用户目录下的Gradle缓存：`rm -rf ~/.gradle/caches`
4. 重新打开Android Studio并同步项目

## 项目结构说明

```
.
├── app/                     # Android应用模块
│   ├── build.gradle.kts     # 模块级构建配置
│   └── src/                 # 源代码目录
│       └── main/            # 主源代码集
│           ├── kotlin/      # Kotlin源代码
│           │   └── com/example/note30/  # 包结构
│           └── res/         # 资源文件
├── docs/                    # 项目文档
├── build.gradle.kts         # 项目级构建配置
├── settings.gradle.kts      # 项目设置
├── gradle/                  # Gradle包装器配置
├── gradlew                  # Gradle包装器脚本（Unix/Linux/macOS）
├── gradlew.bat              # Gradle包装器脚本（Windows）
├── gradle.properties        # Gradle配置属性
└── README.md                # 项目说明文档
```

## 初始化项目

首次打开项目时，Android Studio会自动下载Gradle和项目依赖。如果遇到任何问题，可以在项目根目录运行以下命令：

```
# 在Unix/Linux/macOS上：
./gradlew clean
./gradlew build

# 在Windows上：
gradlew.bat clean
gradlew.bat build
```

## 运行和调试

### 在设备或模拟器上运行

1. 确保已连接Android设备或已启动Android模拟器
2. 在Android Studio工具栏中选择目标设备
3. 点击绿色的"Run"按钮（或按`Shift + F10`）来安装并启动应用

### 调试应用

1. 在代码中设置断点
2. 点击绿色的"Debug"按钮（或按`Shift + F9`）以调试模式启动应用
3. 应用将在断点处暂停，允许您检查变量和执行流程

## 测试

### 运行单元测试

1. 在Android Studio中，右键点击`app/src/test`目录
2. 选择"Run Tests in 'com.example.note30'"来运行所有单元测试

### 运行仪器测试

1. 确保已连接Android设备或已启动Android模拟器
2. 在Android Studio中，右键点击`app/src/androidTest`目录
3. 选择"Run Tests in 'com.example.note30'"来运行所有仪器测试

## 构建

### 生成调试APK

1. 在菜单栏中选择"Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
2. 等待构建过程完成
3. 点击"locate"链接查看生成的APK文件

### 生成发布APK

1. 在菜单栏中选择"Build" > "Generate Signed Bundle / APK"
2. 选择"APK"并点击"Next"
3. 选择或创建密钥库文件和密钥
4. 选择"release"构建类型
5. 点击"Finish"开始构建过程

## 代码质量

### 静态代码分析

1. 在菜单栏中选择"Analyze" > "Inspect Code"
2. 选择要检查的范围（整个项目或特定文件）
3. 点击"OK"开始检查
4. 在"Inspection Results"窗口中查看问题和建议

## 依赖管理

项目使用Gradle进行依赖管理。主要依赖项包括：

- Jetpack Compose：用于构建用户界面
- Room：用于本地数据存储
- WorkManager：用于后台任务调度
- ViewModel和LiveData：用于实现MVVM架构模式

要添加新依赖项，请在`app/build.gradle.kts`文件的`dependencies`块中添加相应的依赖声明，然后同步项目。

## 代码风格

项目遵循Kotlin官方编码规范和Android开发最佳实践。主要特点包括：

- 使用Kotlin语言特性，如数据类、扩展函数等
- 遵循MVVM架构模式
- 使用Jetpack Compose构建声明式UI
- 通过Repository模式管理数据源
- 使用协程处理异步操作

## 贡献

欢迎提交Issue和Pull Request来改进项目。