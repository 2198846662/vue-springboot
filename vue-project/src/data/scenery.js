/**
 * Mock Scenery Data for Tourism Platform
 * High-quality Unsplash images of real destinations
 */

// Categories
export const mockCategories = [
  { id: 'all', name: '全部', icon: 'globe' },
  { id: 'mountain', name: '山川', icon: 'mountain' },
  { id: 'island', name: '海岛', icon: 'umbrella' },
  { id: 'ancient', name: '古城', icon: 'landmark' },
  { id: 'lake', name: '湖泊', icon: 'waves' },
  { id: 'forest', name: '森林', icon: 'trees' },
  { id: 'desert', name: '沙漠', icon: 'sun' },
  { id: 'waterfall', name: '瀑布', icon: 'droplets' }
]

// Scenery List
export const mockSceneryList = [
  {
    id: 1,
    title: '黄山风景区',
    description: '中国著名的山岳风景区，以奇松、怪石、云海、温泉四绝著称。迎客松矗立于悬崖之上，日出时分云海翻涌如波涛，是无数摄影师的朝圣之地。',
    location: '安徽省黄山市',
    category: 'mountain',
    categoryName: '山川',
    images: [
      'https://images.unsplash.com/photo-1503708928676-1cb796a0891e?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1545569341-9eb8b30979d9?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1528164344705-47542687000d?w=800&h=600&fit=crop'
    ],
    rating: 4.8,
    reviewCount: 1256,
    openingHours: '06:30-17:30',
    ticket: '旺季 230元/淡季 150元',
    latitude: 29.7145,
    longitude: 118.158,
    featured: true
  },
  {
    id: 2,
    title: '张家界国家森林公园',
    description: '世界自然遗产，拥有独特的石英砂岩峰林地貌。《阿凡达》取景地，三千多座石峰拔地而起，云雾缭绕间如同悬浮仙境。',
    location: '湖南省张家界市',
    category: 'mountain',
    categoryName: '山川',
    images: [
      'https://images.unsplash.com/photo-1537531383496-f4749b8032cf?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop'
    ],
    rating: 4.7,
    reviewCount: 2341,
    openingHours: '07:00-18:00',
    ticket: '225元',
    latitude: 29.1175,
    longitude: 110.4461,
    featured: true
  },
  {
    id: 3,
    title: '马尔代夫芙花芬岛',
    description: '印度洋上的人间天堂，水晶般透明的泻湖，私密的水下餐厅，世界顶级潜水圣地。每栋别墅都建在海上，推门即可跃入蔚蓝。',
    location: '马尔代夫马累环礁',
    category: 'island',
    categoryName: '海岛',
    images: [
      'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1573843981267-be1999ff37cd?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop'
    ],
    rating: 4.9,
    reviewCount: 876,
    openingHours: '全天开放',
    ticket: '上岛需预约',
    latitude: 4.2144,
    longitude: 73.5093,
    featured: true
  },
  {
    id: 4,
    title: '丽江古城',
    description: '世界文化遗产纳西族古城，小桥流水人家，青石板路蜿蜒。夜晚华灯初上，酒吧街民谣飘荡，感受慢生活的极致。',
    location: '云南省丽江市',
    category: 'ancient',
    categoryName: '古城',
    images: [
      'https://images.unsplash.com/photo-1528181304800-259b08848526?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1547981609-4b6bfe67ca0b?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1528164344705-47542687000d?w=800&h=600&fit=crop'
    ],
    rating: 4.6,
    reviewCount: 3421,
    openingHours: '全天开放',
    ticket: '免费（古城维护费80元）',
    latitude: 26.8721,
    longitude: 100.2297,
    featured: false
  },
  {
    id: 5,
    title: '青海湖',
    description: '中国最大的咸水湖，七月油菜花盛开时金黄一片，与湛蓝湖水相映成趣。湖畔骑行、湖边守候日出，感受大西北的辽阔与壮美。',
    location: '青海省海南藏族自治州',
    category: 'lake',
    categoryName: '湖泊',
    images: [
      'https://images.unsplash.com/photo-1589558248535-8e0f8e7e5d9e?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&h=600&fit=crop'
    ],
    rating: 4.7,
    reviewCount: 1876,
    openingHours: '全天开放',
    ticket: '免费',
    latitude: 36.9989,
    longitude: 99.6347,
    featured: true
  },
  {
    id: 6,
    title: '九寨沟',
    description: '人间仙境，藏族自然保护区。五花海的色彩如梦似幻，诺日朗瀑布雄伟壮观，秋天层林尽染，仿佛打翻了调色盘。',
    location: '四川省阿坝藏族羌族自治州',
    category: 'lake',
    categoryName: '湖泊',
    images: [
      'https://images.unsplash.com/photo-1586659340732-9363b3d84339?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1565018054866-968d574d18a1?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop'
    ],
    rating: 4.9,
    reviewCount: 2134,
    openingHours: '07:30-17:00',
    ticket: '旺季 169元/淡季 80元',
    latitude: 33.1021,
    longitude: 103.9133,
    featured: false
  },
  {
    id: 7,
    title: '西双版纳热带雨林',
    description: '中国唯一热带雨林生态系统，野象谷看亚洲象，傣族园体验泼水节。夜晚的星光夜市是美食天堂，热带水果吃到撑。',
    location: '云南省西双版纳傣族自治州',
    category: 'forest',
    categoryName: '森林',
    images: [
      'https://images.unsplash.com/photo-1558362477-3c67a1f45136?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1440342359743-84fcb8c21f21?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1508193638397-1c4234db14d8?w=800&h=600&fit=crop'
    ],
    rating: 4.5,
    reviewCount: 1567,
    openingHours: '08:00-18:00',
    ticket: '野象谷 65元/基诺山寨 160元',
    latitude: 21.9588,
    longitude: 101.2541,
    featured: false
  },
  {
    id: 8,
    title: '敦煌莫高窟',
    description: '世界文化遗产，东方佛教艺术宝库。735个洞窟，壁画面积超4.5万平方米，藏经洞出土文物震惊世界。丝路文明的璀璨明珠。',
    location: '甘肃省敦煌市',
    category: 'ancient',
    categoryName: '古城',
    images: [
      'https://images.unsplash.com/photo-1569701813229-33284b643e3c?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop'
    ],
    rating: 4.8,
    reviewCount: 982,
    openingHours: '08:00-18:00（需预约）',
    ticket: '238元（含数字中心）',
    latitude: 40.1421,
    longitude: 94.6621,
    featured: false
  },
  {
    id: 9,
    title: '撒哈拉沙漠',
    description: '世界上最大的热带沙漠，三毛笔下的浪漫之地。骑骆驼穿越金色沙丘，夜晚露营看满天繁星，体验柏柏尔人的热情好客。',
    location: '摩洛哥梅尔祖卡',
    category: 'desert',
    categoryName: '沙漠',
    images: [
      'https://images.unsplash.com/photo-1509059852496-f3822ae0571e?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1542401886-65d6c61db217?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?w=800&h=600&fit=crop'
    ],
    rating: 4.6,
    reviewCount: 654,
    openingHours: '全天（建议参加旅行团）',
    ticket: '自由区无需门票',
    latitude: 31.0529,
    longitude: -3.9712,
    featured: false
  },
  {
    id: 10,
    title: '尼亚加拉瀑布',
    description: '世界三大瀑布之一，横跨美加边境。马蹄瀑布垂直落差超过50米，乘坐雾中少女号游船穿越瀑布，水雾扑面而来，震撼人心。',
    location: '加拿大安大略省与美国纽约州',
    category: 'waterfall',
    categoryName: '瀑布',
    images: [
      'https://images.unsplash.com/photo-1489447068241-b3490214e879?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1508193638397-1c4234db14d8?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1509316785289-025f5b846b35?w=800&h=600&fit=crop'
    ],
    rating: 4.7,
    reviewCount: 1876,
    openingHours: '全天开放（夜游更佳）',
    ticket: '观景台免费，游船40美元',
    latitude: 43.0962,
    longitude: -79.0377,
    featured: false
  },
  {
    id: 11,
    title: '瑞士少女峰',
    description: '欧洲之巅，海拔3454米的观景台。乘坐齿轮火车穿越阿尔卑斯山脉，漫步云端，冬季滑雪夏季徒步，一步一景皆是画卷。',
    location: '瑞士因特拉肯',
    category: 'mountain',
    categoryName: '山川',
    images: [
      'https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1527668752968-14dc70a27c95?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop'
    ],
    rating: 4.9,
    reviewCount: 2134,
    openingHours: '08:00-17:00',
    ticket: '登山火车 212瑞士法郎',
    latitude: 46.5476,
    longitude: 7.9676,
    featured: true
  },
  {
    id: 12,
    title: '冰岛黄金圈',
    description: '辛格维利尔国家公园、间歇泉和黄金瀑布三大景点。踏足北美与亚欧板块交界处，看盖锡尔喷出20米高水柱，感受冰岛荒原的神秘力量。',
    location: '冰岛南部',
    category: 'lake',
    categoryName: '湖泊',
    images: [
      'https://images.unsplash.com/photo-1504829857797-ddff29c27927?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1476610182048-b716b8518aae?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop'
    ],
    rating: 4.8,
    reviewCount: 1654,
    openingHours: '全天（部分季节）',
    ticket: '国家公园免费',
    latitude: 64.3127,
    longitude: -20.1244,
    featured: false
  }
]

// Comments by scenery ID
export const mockComments = {
  1: [
    {
      id: 1,
      userId: 'user1',
      userName: '旅行摄影师',
      userAvatar: '摄',
      content: '日出云海真的太美了！建议住在山上等日出，虽然住宿贵但值得。迎客松比想象中还要壮观。',
      createdAt: '2024-03-10T14:30:00Z'
    },
    {
      id: 2,
      userId: 'user2',
      userName: '背包客小王',
      userAvatar: '王',
      content: '去了两次黄山，每次都有新发现。光明顶的星空让我终身难忘。',
      createdAt: '2024-02-28T09:15:00Z'
    },
    {
      id: 3,
      userId: 'user3',
      userName: '美食探索家',
      userAvatar: '食',
      content: '黄山脚下有很多农家乐，毛豆腐和臭鳜鱼必须尝！',
      createdAt: '2024-02-15T18:45:00Z'
    }
  ],
  2: [
    {
      id: 1,
      userId: 'user4',
      userName: '户外爱好者',
      userAvatar: '户',
      content: '天门山玻璃栈道太刺激了！恐高症患者慎入，但风景绝对一流。',
      createdAt: '2024-03-08T11:20:00Z'
    },
    {
      id: 2,
      userId: 'user5',
      userName: '文艺青年',
      userAvatar: '文',
      content: '电影《阿凡达》的灵感来源地，果然名不虚传。那些石峰就像悬浮在云端。',
      createdAt: '2024-02-20T16:00:00Z'
    }
  ],
  3: [
    {
      id: 1,
      userId: 'user6',
      userName: '度假专家',
      userAvatar: '度',
      content: '完美的蜜月圣地！水下餐厅浪漫至极，海水清澈见底能看到魔鬼鱼。',
      createdAt: '2024-03-05T20:30:00Z'
    }
  ],
  5: [
    {
      id: 1,
      userId: 'user7',
      userName: '公路骑士',
      userAvatar: '公',
      content: '环湖自驾太爽了！七月油菜花和湖水的配色绝了，无人机航拍大片感十足。',
      createdAt: '2024-03-01T08:00:00Z'
    },
    {
      id: 2,
      userId: 'user8',
      userName: '星空观测者',
      userAvatar: '星',
      content: '青海湖的星空是我见过最美的，没有光污染，银河清晰可见。',
      createdAt: '2024-02-25T23:15:00Z'
    }
  ]
}

// Featured scenery for hero
export const featuredScenery = mockSceneryList.filter(s => s.featured)
