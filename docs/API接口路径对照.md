# API接口路径对照表

## 前端调用路径 vs 后端Controller路径

### 认证相关
- ✅ `/api/login` → `LoginController.postMapping("/login")`
- ✅ `/api/register` → `LoginController.postMapping("/register")` (已添加)

### 打卡相关
- ✅ `/api/attendance/check-in` → `AttendanceController.postMapping("/check-in")`
- ✅ `/api/attendance/check-out` → `AttendanceController.postMapping("/check-out")`
- ✅ `/api/attendance/today` → `AttendanceController.getMapping("/today")` (返回404是正常的，表示今天没有打卡)
- ✅ `/api/attendance/records` → `AttendanceController.getMapping("/records")`
- ✅ `/api/attendance/statistics` → `AttendanceController.getMapping("/statistics")`

### 申请相关
- ✅ `/api/applications` → `ApplicationController.getMapping()` (GET)
- ✅ `/api/applications` → `ApplicationController.postMapping()` (POST)
- ✅ `/api/applications/{id}/cancel` → `ApplicationController.postMapping("/{id}/cancel")`
- ✅ `/api/applications/reissue/limit` → `ApplicationController.getMapping("/reissue/limit")`

### 审批相关
- ✅ `/api/approval/pending` → `ApprovalController.getMapping("/pending")`
- ✅ `/api/approval/applications/{id}/review` → `ApprovalController.postMapping("/applications/{id}/review")`

### 管理员相关
- ✅ `/api/admin/employees` → `AdminController.getMapping("/employees")` (GET)
- ✅ `/api/admin/employees` → `AdminController.postMapping("/employees")` (POST)
- ✅ `/api/admin/employees/{id}` → `AdminController.putMapping("/employees/{id}")` (PUT)
- ✅ `/api/admin/employees/{id}/reset-password` → `AdminController.postMapping("/employees/{id}/reset-password")`
- ✅ `/api/admin/employees/{id}` → `AdminController.deleteMapping("/employees/{id}")` (DELETE)
- ✅ `/api/admin/departments` → `AdminController.getMapping("/departments")` (GET)
- ✅ `/api/admin/departments` → `AdminController.postMapping("/departments")` (POST)
- ✅ `/api/admin/departments/{id}` → `AdminController.putMapping("/departments/{id}")` (PUT)
- ✅ `/api/admin/departments/{id}` → `AdminController.deleteMapping("/departments/{id}")` (DELETE)
- ✅ `/api/admin/positions` → `AdminController.getMapping("/positions")` (GET)
- ✅ `/api/admin/positions` → `AdminController.postMapping("/positions")` (POST)
- ✅ `/api/admin/positions/{id}` → `AdminController.putMapping("/positions/{id}")` (PUT)
- ✅ `/api/admin/positions/{id}` → `AdminController.deleteMapping("/positions/{id}")` (DELETE)
- ✅ `/api/admin/attendance` → `AdminController.getMapping("/attendance")`
- ✅ `/api/admin/applications/{id}/approve` → `AdminController.postMapping("/applications/{id}/approve")`

### 员工搜索
- ✅ `/api/employees/search` → `EmployeeController.getMapping("/search")`

## 注意事项

1. **404错误处理**：
   - `/api/attendance/today` 返回404是正常的，表示今天还没有打卡记录
   - 前端代码已经正确处理了这种情况（返回null）

2. **如果遇到404错误**：
   - 检查后端服务是否正在运行（端口8080）
   - 检查浏览器控制台的完整错误信息，确认是哪个接口返回404
   - 检查后端日志，看是否有请求到达

3. **测试步骤**：
   - 重启后端服务
   - 打开浏览器开发者工具（F12）
   - 查看Network标签，找到返回404的请求
   - 检查请求的URL是否与后端Controller路径匹配




















