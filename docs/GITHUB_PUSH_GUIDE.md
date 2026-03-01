# GitHub 提交与推送问题处理指南

## 1. 常见错误：无法连接 GitHub（443）

**现象**：执行 `git push origin <分支>` 时报错：

```
fatal: unable to access 'https://github.com/xxx/ly-factmesh.git/': 
Failed to connect to github.com port 443 after xxx ms: Couldn't connect to server
```

**常见原因**：

- 当前环境（如 IDE 内置终端、CI、内网）无法访问 GitHub（防火墙/代理/网络限制）
- 使用 HTTPS 远程地址时，本机未配置代理或 DNS 异常

---

## 2. 推荐做法：在本机终端推送

代码已成功提交到**本地**仓库，只需在你自己的电脑上（能正常打开 https://github.com 的环境）执行推送：

```powershell
# 进入项目根目录
cd d:\Users\73559\IdeaProjects\LY-FactMesh\ly-factmesh

# 查看当前分支
git branch --show-current

# 推送到 GitHub（分支名与上面一致，一般为 integration/admin-module-api）
git push origin integration/admin-module-api
```

若当前分支就是你要推送的分支，也可以：

```powershell
git push -u origin HEAD
```

---

## 3. 若本机也连不上 GitHub

### 3.1 使用代理（本机已开科学上网）

在项目目录下设置 Git 使用代理（端口按你本机代理软件为准，常见为 7890 或 1080）：

```powershell
# HTTP 代理（多数情况用这个）
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy http://127.0.0.1:7890

# 仅对 GitHub 设置（不影响其他域名）
git config --global http.https://github.com.proxy http://127.0.0.1:7890
```

然后重试：

```powershell
git push origin integration/admin-module-api
```

取消代理：

```powershell
git config --global --unset http.proxy
git config --global --unset https.proxy
```

### 3.2 改用 SSH（推荐，避免 HTTPS 端口被拦）

1. **查看当前远程地址**  
   ```powershell
   git remote -v
   ```  
   若显示 `https://github.com/xxx/ly-factmesh.git`，说明是 HTTPS。

2. **本机已配置 SSH 公钥到 GitHub 时**，改为 SSH 地址并推送：  
   ```powershell
   # 将 xxx 换成你的 GitHub 用户名或组织名
   git remote set-url origin git@github.com:quxiangshun/ly-factmesh.git
   git remote -v
   git push origin integration/admin-module-api
   ```

3. **未配置 SSH 时**：在 GitHub 网页 → Settings → SSH and GPG keys → New SSH key，按页面说明在本机生成并粘贴公钥后再执行上面 `set-url` 和 `push`。

---

## 4. 认证失败（401/403）

**现象**：能连上 GitHub，但 push 时报 `Authentication failed` 或 `403`。

- **HTTPS**：GitHub 已不再支持账号密码，需使用 **Personal Access Token (PAT)**。  
  - 在 GitHub → Settings → Developer settings → Personal access tokens 生成 Token（勾选 `repo`）。  
  - 推送时密码处粘贴该 Token。  
  - 或配置凭据存储：  
    ```powershell
    git config --global credential.helper store
    ```  
    再执行一次 `git push` 并输入用户名与 Token，之后会保存。

- **SSH**：确保本机有 SSH key 且公钥已添加到 GitHub，且 `git remote` 为 `git@github.com:...`。

---

## 5. 一键检查与推送脚本（本机执行）

在项目根目录创建或使用以下 PowerShell 片段，在本机执行：

```powershell
# 检查远程与分支
Write-Host "当前分支: $(git branch --show-current)"
Write-Host "远程地址: $(git remote get-url origin)"

# 未推送的提交数
$count = (git rev-list origin/integration/admin-module-api..HEAD 2>$null | Measure-Object -Line).Lines
if ($count -gt 0) { Write-Host "待推送提交: $count 个" } else { Write-Host "无待推送提交或远程分支不存在" }

# 执行推送
git push origin integration/admin-module-api
```

若远程分支名不同，把 `integration/admin-module-api` 改成你实际要推送的分支名。

---

## 6. 小结

| 情况           | 建议操作 |
|----------------|----------|
| 本地已提交，需推送到 GitHub | 在本机终端执行 `git push origin <当前分支>` |
| 本机访问 GitHub 需代理     | 配置 `http.https://github.com.proxy` 后再 push |
| 想避免 443/HTTPS 问题      | 改用 SSH：`git remote set-url origin git@github.com:用户/ly-factmesh.git` 再 push |
| 推送时报 401/403           | HTTPS 用 PAT 代替密码；SSH 检查公钥是否已添加到 GitHub |

当前仓库的提交已在本地，按上述任一种方式在本机成功执行一次 `git push` 即可同步到 GitHub。
