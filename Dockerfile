# 第一阶段: 构建 Java_common
# 使用 Maven 官方镜像作为基础镜像
FROM maven:3-openjdk-17 AS build-common

# 复制 Java_common 项目文件到容器中
COPY WYN-GraduationProject-common/java_common /Java_common

# 设置工作目录
WORKDIR /Java_common

# 运行 Maven 命令来安装依赖包到本地 Maven 仓库
RUN mvn install

# 第二阶段: 构建 Gateway
# 再次使用 Maven 镜像
FROM maven:3-openjdk-17 AS build-authService

# 从第一阶段拷贝已安装的依赖包
COPY --from=build-common /root/.m2 /root/.m2

# 复制 Gateway 项目文件到容器中
COPY . /gateway

# 设置工作目录
WORKDIR /gateway

# 构建 Gateway 项目，包括测试和打包
RUN mvn package

# 第三阶段: 创建运行镜像
# 使用只包含 JRE 的 OpenJDK 镜像
FROM openjdk:17-jdk-slim

# 从第二阶段拷贝构建好的可执行 Jar 文件到运行目录
COPY --from=build-authService /gateway/target/gateway-0.0.1-SNAPSHOT.jar /app/gateway.jar

# 设置容器的工作目录
WORKDIR /app

# 定义容器启动时执行的命令
CMD ["java", "-jar", "gateway.jar"]
