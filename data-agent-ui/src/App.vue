<template>
  <div class="app-container">
    <header class="app-header">
      <div class="logo">
        <el-icon :size="28"><DataAnalysis /></el-icon>
        <span class="logo-text">NL2SQL 智能数据分析</span>
      </div>
      <div class="header-actions">
        <el-select v-model="currentDatasourceId" placeholder="选择数据源" size="large" @change="onDatasourceChange">
          <el-option
            v-for="ds in datasources"
            :key="ds.id"
            :label="ds.name"
            :value="ds.id"
          />
        </el-select>
        <el-button type="primary" @click="showDatasourceDialog = true">
          <el-icon><Plus /></el-icon>
          数据源
        </el-button>
      </div>
    </header>

    <main class="app-main">
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <span v-if="!sidebarCollapsed">会话列表</span>
          <el-button :icon="sidebarCollapsed ? Expand : Fold" link @click="sidebarCollapsed = !sidebarCollapsed" />
        </div>
        <div class="session-list" v-if="!sidebarCollapsed">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSessionId === session.id }"
            @click="selectSession(session)"
          >
            <el-icon><ChatDotRound /></el-icon>
            <span class="session-title">{{ session.title || '新会话' }}</span>
            <el-button
              :icon="Delete"
              link
              size="small"
              @click.stop="deleteSession(session.id)"
            />
          </div>
          <el-empty v-if="sessions.length === 0" description="暂无会话" :image-size="60" />
        </div>
        <div class="sidebar-footer">
          <el-button class="new-session-btn" type="primary" @click="createNewSession">
            <el-icon><Plus /></el-icon>
            <span v-if="!sidebarCollapsed">新建会话</span>
          </el-button>
        </div>
      </aside>

      <section class="chat-area">
        <div class="chat-header">
          <h3>{{ currentSessionTitle }}</h3>
          <div class="chat-actions">
            <el-tag v-if="isStreaming" type="warning" effect="dark">
              <el-icon class="is-loading"><Loading /></el-icon>
              AI思考中...
            </el-tag>
          </div>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-icon">
              <el-icon :size="64"><ChatLineSquare /></el-icon>
            </div>
            <h2>欢迎使用 NL2SQL 智能分析助手</h2>
            <p>用自然语言描述你的数据分析需求，我会帮你生成 SQL 查询</p>
            <div class="quick-queries">
              <el-tag
                v-for="(query, index) in quickQueries"
                :key="index"
                class="quick-query-tag"
                @click="sendQuery(query)"
              >
                {{ query }}
              </el-tag>
            </div>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div class="message-avatar">
              <el-icon v-if="msg.role === 'user'" :size="24"><User /></el-icon>
              <el-icon v-else :size="24"><Monitor /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div v-if="msg.role === 'assistant'" class="markdown-content" v-html="renderMarkdown(msg.content)"></div>
                <pre v-else class="user-message">{{ msg.content }}</pre>
              </div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>

          <div v-if="isStreaming && streamingContent" class="message assistant">
            <div class="message-avatar">
              <el-icon :size="24"><Monitor /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="markdown-content" v-html="renderMarkdown(streamingContent)"></div>
              </div>
              <div class="streaming-indicator">
                <span class="loading-dots">AI思考中</span>
              </div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <div class="input-wrapper">
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="2"
              placeholder="输入你的数据分析问题... (Shift+Enter换行, Enter发送)"
              @keydown.enter.exact.prevent="handleEnterKey"
              @keydown.enter.shift.exact="handleShiftEnter"
              :disabled="!currentDatasourceId || isStreaming"
            />
            <el-button
              type="primary"
              :icon="Promotion"
              :disabled="!userInput.trim() || !currentDatasourceId || isStreaming"
              @click="sendMessage"
              circle
            />
          </div>
          <div class="input-hints">
            <span>支持自然语言提问</span>
            <span class="divider">|</span>
            <span>自动生成 SQL 查询</span>
          </div>
        </div>
      </section>
    </main>

    <el-dialog
      v-model="showDatasourceDialog"
      title="数据源管理"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="datasource-list">
        <el-table :data="datasources" stripe style="width: 100%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag type="info">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="host" label="主机" />
          <el-table-column prop="databaseName" label="数据库" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="testConnection(row.id)">测试</el-button>
              <el-button type="danger" link @click="deleteDatasource(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider content-position="center">添加数据源</el-divider>

      <el-form :model="datasourceForm" label-width="100px" :rules="datasourceRules" ref="datasourceFormRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="datasourceForm.name" placeholder="给数据源起个名字" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="datasourceForm.type" placeholder="选择数据库类型">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机" prop="host">
          <el-input v-model="datasourceForm.host" placeholder="127.0.0.1" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="datasourceForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="数据库" prop="databaseName">
          <el-input v-model="datasourceForm.databaseName" placeholder="数据库名称" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="datasourceForm.username" placeholder="root" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="datasourceForm.password" type="password" show-password placeholder="密码" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDatasourceDialog = false">取消</el-button>
        <el-button type="primary" @click="addDatasource" :loading="datasourceLoading">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import axios from 'axios'
import {
  DataAnalysis, ChatDotRound, User, Plus, Delete,
  Promotion, ChatLineSquare, Expand, Fold, Loading, Monitor, Connection
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = '/api'

marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true
})

const quickQueries = [
  '查询本月销售额最高的产品',
  '统计每个部门的员工数量',
  '找出订单量增长最快的月份'
]

const messagesContainer = ref(null)
const messages = ref([])
const userInput = ref('')
const sessions = ref([])
const currentSessionId = ref(null)
const currentSessionTitle = ref('新会话')
const isStreaming = ref(false)
const streamingContent = ref('')
const showDatasourceDialog = ref(false)
const sidebarCollapsed = ref(false)
const datasourceLoading = ref(false)
const datasourceFormRef = ref(null)

const datasources = ref([])
const currentDatasourceId = ref(null)

const datasourceForm = reactive({
  name: '',
  type: 'MYSQL',
  host: '127.0.0.1',
  port: 3306,
  databaseName: '',
  username: 'root',
  password: ''
})

const datasourceRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名称', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const renderMarkdown = (content) => {
  if (!content) return ''
  return marked(content)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleEnterKey = (e) => {
  sendMessage()
}

const handleShiftEnter = (e) => {
}

const sendQuery = (query) => {
  if (!currentDatasourceId.value) {
    ElMessage.warning('请先选择数据源')
    return
  }
  userInput.value = query
  sendMessage()
}

const sendMessage = async () => {
  if (!userInput.value.trim() || !currentDatasourceId.value || isStreaming.value) return

  const query = userInput.value.trim()
  userInput.value = ''

  if (!currentSessionId.value) {
    await createNewSession()
  }

  const userMsg = {
    role: 'user',
    content: query,
    createTime: new Date().toISOString()
  }
  messages.value.push(userMsg)
  scrollToBottom()

  await saveMessage(userMsg)

  isStreaming.value = true
  streamingContent.value = ''

  try {
    const response = await fetch(
      `${API_BASE}/stream/search?datasourceId=${currentDatasourceId.value}&query=${encodeURIComponent(query)}&nl2sqlOnly=false`,
      {
        headers: { 'Accept': 'text/event-stream' }
      }
    )

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let fullContent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.substring(6))
            if (data.text && !data.complete && !data.error) {
              fullContent += data.text
              streamingContent.value = fullContent
              scrollToBottom()
            } else if (data.complete) {
              messages.value.push({
                role: 'assistant',
                content: fullContent,
                createTime: new Date().toISOString()
              })
              streamingContent.value = ''
              isStreaming.value = false
              scrollToBottom()
            } else if (data.error) {
              ElMessage.error(data.text || '查询出错')
              streamingContent.value = ''
              isStreaming.value = false
            }
          } catch (e) {
            console.error('Parse SSE error:', e)
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('请求失败: ' + error.message)
    streamingContent.value = ''
    isStreaming.value = false
  }
}

const saveMessage = async (message) => {
  if (!currentSessionId.value) return
  try {
    await axios.post(`${API_BASE}/sessions/${currentSessionId.value}/messages`, {
      role: message.role,
      content: message.content,
      messageType: 'text',
      titleNeeded: messages.value.filter(m => m.role === 'user').length === 1
    })
  } catch (error) {
    console.error('Save message error:', error)
  }
}

const createNewSession = async () => {
  if (!currentDatasourceId.value) {
    ElMessage.warning('请先选择数据源')
    return
  }
  try {
    const { data } = await axios.post(`${API_BASE}/datasource/${currentDatasourceId.value}/sessions`, {
      title: '新会话'
    })
    currentSessionId.value = data.id
    currentSessionTitle.value = data.title || '新会话'
    messages.value = []
    sessions.value.unshift(data)
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

const selectSession = async (session) => {
  currentSessionId.value = session.id
  currentSessionTitle.value = session.title || '新会话'
  try {
    const { data } = await axios.get(`${API_BASE}/sessions/${session.id}/messages`)
    messages.value = data.map(m => ({
      role: m.role,
      content: m.content,
      createTime: m.createTime
    }))
    scrollToBottom()
  } catch (error) {
    console.error('Load messages error:', error)
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`${API_BASE}/datasource/${currentDatasourceId.value}/sessions`)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      currentSessionTitle.value = '新会话'
      messages.value = []
    }
    ElMessage.success('会话已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除会话失败')
    }
  }
}

const onDatasourceChange = async (dsId) => {
  currentDatasourceId.value = dsId
  currentSessionId.value = null
  currentSessionTitle.value = '新会话'
  messages.value = []
  await loadSessions()
}

const loadDatasources = async () => {
  try {
    const { data } = await axios.get(`${API_BASE}/datasources`)
    datasources.value = data
    if (data.length > 0) {
      currentDatasourceId.value = data[0].id
      await loadSessions()
    }
  } catch (error) {
    console.error('Load datasources error:', error)
  }
}

const loadSessions = async () => {
  if (!currentDatasourceId.value) return
  try {
    const { data } = await axios.get(`${API_BASE}/datasource/${currentDatasourceId.value}/sessions`)
    sessions.value = data
  } catch (error) {
    console.error('Load sessions error:', error)
  }
}

const addDatasource = async () => {
  if (!datasourceFormRef.value) return
  try {
    await datasourceFormRef.value.validate()
    datasourceLoading.value = true
    const { data } = await axios.post(`${API_BASE}/datasources`, datasourceForm)
    datasources.value.push(data)
    currentDatasourceId.value = data.id
    showDatasourceDialog.value = false
    ElMessage.success('数据源添加成功')
    Object.assign(datasourceForm, {
      name: '',
      type: 'MYSQL',
      host: '127.0.0.1',
      port: 3306,
      databaseName: '',
      username: 'root',
      password: ''
    })
  } catch (error) {
    if (error !== false) {
      ElMessage.error('添加数据源失败')
    }
  } finally {
    datasourceLoading.value = false
  }
}

const testConnection = async (id) => {
  try {
    await axios.post(`${API_BASE}/datasources/${id}/test`)
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error('连接测试失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteDatasource = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个数据源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`${API_BASE}/datasources/${id}`)
    datasources.value = datasources.value.filter(d => d.id !== id)
    if (currentDatasourceId.value === id) {
      currentDatasourceId.value = datasources.value[0]?.id || null
      if (!currentDatasourceId.value) {
        sessions.value = []
        messages.value = []
      }
    }
    ElMessage.success('数据源已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除数据源失败')
    }
  }
}

onMounted(() => {
  loadDatasources()
})
</script>

<style lang="scss" scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-dark);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background: linear-gradient(180deg, rgba(22, 27, 34, 0.95) 0%, rgba(13, 17, 23, 0.95) 100%);
  border-bottom: 1px solid var(--border-color);
  backdrop-filter: blur(10px);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--primary-color);
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff, #79b8ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  transition: width 0.3s ease;

  &.collapsed {
    width: 60px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);

  &:hover {
    background: var(--bg-input);
    color: var(--text-primary);
  }

  &.active {
    background: var(--primary-color);
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.1));
    border: 1px solid rgba(64, 158, 255, 0.3);
    color: var(--text-primary);
  }

  .session-title {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.new-session-btn {
  width: 100%;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);

  h3 {
    font-size: 16px;
    font-weight: 500;
    color: var(--text-primary);
  }
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: var(--text-secondary);

  .welcome-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 120px;
    height: 120px;
    margin-bottom: 24px;
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
    border-radius: 24px;
    color: var(--primary-color);
  }

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #c9d1d9, #8b949e);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    font-size: 14px;
    margin-bottom: 24px;
  }
}

.quick-queries {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 600px;
}

.quick-query-tag {
  cursor: pointer;
  padding: 8px 16px;
  font-size: 13px;
  transition: all 0.2s ease;
  border-radius: 20px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  &.user {
    flex-direction: row-reverse;

    .message-bubble {
      background: linear-gradient(135deg, var(--primary-color), #66b1ff);
      color: #fff;
    }
  }

  &.assistant {
    .message-bubble {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
    }
  }
}

.message-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;

  .user & {
    background: linear-gradient(135deg, var(--primary-color), #66b1ff);
    color: #fff;
  }

  .assistant & {
    background: linear-gradient(135deg, #2d333b, #373e47);
    color: var(--primary-color);
  }
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;

  .user-message {
    margin: 0;
    font-family: inherit;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.message-time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);

  .user & {
    text-align: right;
  }
}

.streaming-indicator {
  margin-top: 8px;
  font-size: 12px;
  color: var(--primary-color);
}

.input-area {
  padding: 16px 24px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;

  .el-textarea {
    flex: 1;
  }
}

.input-hints {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);

  .divider {
    color: var(--border-color);
  }
}

.datasource-list {
  margin-bottom: 16px;
}

.markdown-content {
  :deep(h1), :deep(h2), :deep(h3) {
    margin: 16px 0 8px;
    color: var(--text-primary);
  }

  :deep(p) {
    margin: 8px 0;
  }

  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 24px;
  }

  :deep(code) {
    background: var(--bg-input);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Fira Code', monospace;
    font-size: 13px;
  }

  :deep(pre) {
    margin: 12px 0;

    code {
      display: block;
      padding: 16px;
      background: #0d1117;
      border-radius: 8px;
      overflow-x: auto;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;

    th, td {
      padding: 8px 12px;
      border: 1px solid var(--border-color);
      text-align: left;
    }

    th {
      background: var(--bg-input);
      font-weight: 600;
    }
  }
}
</style>
