// API配置文件
const isProd = process.env.NODE_ENV === 'production';

// 开发环境使用代理，生产环境使用实际域名
// 确保API前缀的一致性
const baseURL = '/api';

export default {
  baseURL
};
