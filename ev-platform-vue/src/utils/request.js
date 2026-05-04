import axios from 'axios'
// 1. 引入消息提示组件
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['token'] = token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

request.interceptors.response.use(
    response => {
        return response.data;
    },
    error => {
        if (error.response && error.response.status === 401) {
            // 2. 弹出一个友好的提示告诉用户发生了什么
            ElMessage.warning('您的登录状态已过期，请重新登录！');

            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('nickname');
            localStorage.removeItem('role');
            localStorage.removeItem('userInfo');

            // 3. 延迟 1.5 秒再跳转，让用户看清提示框
            setTimeout(() => {
                window.location.href = '/';
            }, 1500);
        }
        return Promise.reject(error);
    }
)

export default request
