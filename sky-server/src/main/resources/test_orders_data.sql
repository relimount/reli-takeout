-- 测试数据：orders 表
-- 订单状态：1 待付款 2 待接单 3 已接单 4 派送中 5 已完成 6 已取消
-- 支付状态：0 未支付 1 已支付 2 退款
-- 支付方式：1 微信，2 支付宝

-- 假设已存在的用户 ID: 1, 2, 3
-- 假设已存在的地址簿 ID: 1, 2, 3

-- ==================== 待付款订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, pay_method, pay_status, amount, remark, userName, phone, address, consignee, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303001', 1, 1, 1, '2026-03-03 08:30:00', 1, 0, 45.50, '请尽快配送', '张三', '13800138001', '北京市朝阳区 XX 街道 XX 号', '张三', 1, 2, 2, 1),
('ORD20260303002', 1, 2, 2, '2026-03-03 09:15:00', 2, 0, 68.00, '多放辣椒', '李四', '13800138002', '北京市海淀区 XX 路 XX 小区', '李四', 0, 3, 0, 0);

-- ==================== 待接单订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, userName, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303003', 2, 1, 1, '2026-03-03 10:00:00', '2026-03-03 10:01:00', 1, 1, 52.80, '不要香菜', '张三', '13800138001', '北京市朝阳区 XX 街道 XX 号', '张三', '2026-03-03 11:00:00', 1, 2, 2, 1),
('ORD20260303004', 2, 3, 3, '2026-03-03 10:30:00', '2026-03-03 10:31:00', 1, 1, 89.50, NULL, '王五', '13800138003', '北京市丰台区 XX 街 XX 号楼', '王五', '2026-03-03 11:30:00', 0, 4, 3, 1);

-- ==================== 已接单订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, userName, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303005', 3, 2, 2, '2026-03-03 11:00:00', '2026-03-03 11:02:00', 2, 1, 125.00, '趁热送', '李四', '13800138002', '北京市海淀区 XX 路 XX 小区', '李四', '2026-03-03 12:00:00', 1, 5, 4, 1),
('ORD20260303006', 3, 1, 3, '2026-03-03 11:20:00', '2026-03-03 11:21:00', 1, 1, 36.90, NULL, '张三', '13800138001', '北京市丰台区 XX 街 XX 号楼', '张三', '2026-03-03 12:20:00', 1, 1, 1, 1);

-- ==================== 派送中订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, userName, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303007', 4, 3, 1, '2026-03-03 11:45:00', '2026-03-03 11:47:00', 1, 1, 78.60, '放门口', '王五', '13800138003', '北京市朝阳区 XX 街道 XX 号', '王五', '2026-03-03 12:30:00', 1, 3, 2, 1),
('ORD20260303008', 4, 2, 3, '2026-03-03 12:00:00', '2026-03-03 12:02:00', 2, 1, 95.20, '打电话联系', '李四', '13800138002', '北京市丰台区 XX 街 XX 号楼', '李四', '2026-03-03 13:00:00', 1, 4, 3, 0);

-- ==================== 已完成订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, userName, phone, address, consignee, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303009', 5, 1, 2, '2026-03-03 08:00:00', '2026-03-03 08:01:00', 1, 1, 42.00, NULL, '张三', '13800138001', '北京市海淀区 XX 路 XX 小区', '张三', '2026-03-03 09:00:00', 1, '2026-03-03 08:50:00', 2, 2, 1),
('ORD20260303010', 5, 2, 1, '2026-03-03 09:30:00', '2026-03-03 09:32:00', 2, 1, 156.80, '谢谢', '李四', '13800138002', '北京市朝阳区 XX 街道 XX 号', '李四', '2026-03-03 10:30:00', 1, '2026-03-03 10:20:00', 6, 5, 1),
('ORD20260303011', 5, 3, 3, '2026-03-03 10:15:00', '2026-03-03 10:16:00', 1, 1, 63.50, NULL, '王五', '13800138003', '北京市丰台区 XX 街 XX 号楼', '王五', '2026-03-03 11:15:00', 1, '2026-03-03 11:05:00', 3, 3, 1),
('ORD20260302001', 5, 1, 1, '2026-03-02 18:00:00', '2026-03-02 18:02:00', 1, 1, 88.00, '昨天订单', '张三', '13800138001', '北京市朝阳区 XX 街道 XX 号', '张三', '2026-03-02 19:00:00', 1, '2026-03-02 18:55:00', 4, 4, 1),
('ORD20260302002', 5, 2, 2, '2026-03-02 12:30:00', '2026-03-02 12:31:00', 2, 1, 210.50, '昨天午餐', '李四', '13800138002', '北京市海淀区 XX 路 XX 小区', '李四', '2026-03-02 13:30:00', 1, '2026-03-02 13:25:00', 8, 6, 0);

-- ==================== 已取消订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, cancel_time, cancel_reason, pay_method, pay_status, amount, remark, userName, phone, address, consignee, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303012', 6, 1, 2, '2026-03-03 13:00:00', '2026-03-03 13:05:00', '顾客改变主意', 1, 0, 55.00, NULL, '张三', '13800138001', '北京市海淀区 XX 路 XX 小区', '张三', 0, 2, 2, 1),
('ORD20260303013', 6, 3, 1, '2026-03-03 13:30:00', '2026-03-03 13:35:00', '送货时间太长', 2, 2, 72.30, '申请退款', '王五', '13800138003', '北京市朝阳区 XX 街道 XX 号', '王五', 1, 3, 3, 0);

-- ==================== 退款订单 ====================
INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, cancel_time, cancel_reason, pay_method, pay_status, amount, remark, userName, phone, address, consignee, delivery_status, pack_amount, tableware_number, tableware_status)
VALUES 
('ORD20260303014', 6, 2, 3, '2026-03-03 14:00:00', '2026-03-03 14:01:00', '2026-03-03 14:30:00', '食品质量问题', 1, 2, 98.00, '要求退款', '李四', '13800138002', '北京市丰台区 XX 街 XX 号楼', '李四', 1, 4, 4, 1);
