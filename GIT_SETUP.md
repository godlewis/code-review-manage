# Git 和 GitHub 设置指南

## 已完成的步骤

1. 初始化本地 Git 仓库
2. 配置 Git 用户信息
3. 添加项目文件并创建初始提交
4. 在 GitHub 上创建仓库 (https://github.com/godlewis/code-review-manage)
5. 添加 GitHub 仓库作为远程源

## 待完成步骤

由于网络连接问题，尚未将本地代码推送到 GitHub。

### 手动推送命令

在终端中执行以下命令:

```bash
cd d:\temp\code-review-manage
git push -u origin master
```

如果仍然遇到网络问题，请尝试以下解决方案:

1. 检查网络连接
2. 配置 Git 代理 (如果在公司网络中):
   ```bash
   git config --global http.proxy http://proxy.server:port
   ```
3. 或者使用 SSH 方式 (需要先配置 SSH 密钥):
   ```bash
   git remote set-url origin git@github.com:godlewis/code-review-manage.git
   git push -u origin master
   ```