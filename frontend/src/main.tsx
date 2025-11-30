import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import ErrorBoundary from './components/ErrorBoundary'
import './styles.css'

try {
  const rootElement = document.getElementById('root')
  if (!rootElement) {
    throw new Error('Root element not found')
  }

  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <ErrorBoundary>
        <App />
      </ErrorBoundary>
    </React.StrictMode>,
  )
} catch (error) {
  console.error('Failed to render app:', error)
  const rootElement = document.getElementById('root')
  if (rootElement) {
    rootElement.innerHTML = `
      <div style="display: flex; align-items: center; justify-content: center; min-height: 100vh; background: #050505; color: #ffffff; padding: 20px; flex-direction: column;">
        <h1>应用启动失败</h1>
        <p style="color: rgba(255, 255, 255, 0.7); margin-top: 10px;">${error instanceof Error ? error.message : '未知错误'}</p>
        <p style="color: rgba(255, 255, 255, 0.5); margin-top: 20px; font-size: 12px;">请检查浏览器控制台获取更多信息</p>
      </div>
    `
  }
}
