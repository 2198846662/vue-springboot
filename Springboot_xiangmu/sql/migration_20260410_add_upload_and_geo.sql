-- 增量迁移：本地图片路径 + 景点经纬度
-- 适用于已存在 tourism.scenery 表的环境

USE tourism;

ALTER TABLE scenery
    MODIFY COLUMN cover_image VARCHAR(500) COMMENT '本地上传后保存访问路径，如 /uploads/xxx.jpg';

ALTER TABLE scenery
    ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,6) NULL AFTER open_time,
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(10,6) NULL AFTER latitude;

-- 示例：为已有景点补充经纬度（按名称匹配）
UPDATE scenery SET latitude = 30.131129, longitude = 118.182763 WHERE name = '黄山风景区';
UPDATE scenery SET latitude = 39.916345, longitude = 116.397155 WHERE name = '故宫博物院';
UPDATE scenery SET latitude = 31.144339, longitude = 121.657410 WHERE name = '迪士尼乐园';

-- 如需要切换为本地图片，可将 cover_image 更新为 /uploads/文件名
-- UPDATE scenery SET cover_image = '/uploads/your-file.jpg' WHERE id = 1;
