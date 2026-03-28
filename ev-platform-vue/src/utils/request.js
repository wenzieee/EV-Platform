import axios from 'axios'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// ====== 请求拦截器：发请求前会自动执行这里的代码 ======
request.interceptors.request.use(
    config => {
        // 尝试从浏览器的 localStorage 中获取 token
        const token = localStorage.getItem('token');
        if (token) {
            // 如果有 token，就放到请求头（Header）里带给后端
            config.headers['token'] = token; 
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// ====== 响应拦截器：后端返回数据后会自动执行这里的代码 ======
request.interceptors.response.use(
    response => {
        return response.data; 
    },
    error => {
        return Promise.reject(error);
    }
)

export default request