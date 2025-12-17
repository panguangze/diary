#!/bin/bash

# 配置参数
SOURCE_DIR="/mnt/c/Users/gzpan2/AndroidStudioProjects/diary/app/src/main/java/com/love/diary"    # 修改为你的源目录路径
TARGET_DIR="/mnt/c/Users/gzpan2/AndroidStudioProjects/test/" # 修改为你的目标目录路径

# 创建目标目录
mkdir -p "$TARGET_DIR"

echo "开始搜索和复制.kt文件..."

# 计数器
file_count=0

# 使用find查找所有.kt文件
find "$SOURCE_DIR" -type f -name "*.kt" | while read -r file; do
    # 获取相对路径（去掉源目录前缀）
    rel_path="${file#$SOURCE_DIR/}"
    
    # 获取文件名（不含路径）
    filename=$(basename "$file")
    
    # 如果文件直接在源目录下（没有子目录）
    if [[ "$file" == "$SOURCE_DIR/$filename" ]]; then
        # 直接复制到目标目录
        cp "$file" "$TARGET_DIR/"
        echo "已复制: $filename"
    else
        # 如果有子目录结构，将文件复制到目标目录的相应位置
        # 这里我们选择将所有文件都复制到目标目录根目录（平铺）
        cp "$file" "$TARGET_DIR/"
        echo "已复制: $rel_path"
    fi
    
    ((file_count++))
done

echo ""
echo "======================================"
echo "复制完成！"
echo "共找到并复制了 $file_count 个.kt文件"
echo "所有文件都已复制到: $TARGET_DIR"
echo "======================================"
