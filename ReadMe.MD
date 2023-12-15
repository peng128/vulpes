<img src=".idea/icon.png" width = "100" height = "120"><br>

### 环境
需要 java 17

### 测试方法
vuples-lanucher 模块下测试方法TestMain可直接运行即可启动服务。端口默认为13000.

可以使用mysql客户端执行命令 
```mysql -h127.0.0.1 -P13000```来连接服务端。目前没有做权限验证。测试数据在同模块resources下。

可测试sql
```select "name", age, gender, "type" from table1 t1 join table2 t2 on t1.id = t2.id```

### 测试数据生成
在```vulpes-benchmark```模块中, 运行测试用例TpchGeneratorTests可以生成Arrow类型的测试数据在项目目录下的tpch目录.