<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'
import { adminProfile, adminUserList, adminUserSetStatus, adminUserSetPermission, adminUserDelete } from '../api/auth'
import { archiveAdminList, archiveCreate, archiveUpdate, archiveDelete, archiveBatchStatus } from '../api/archive'

const router = useRouter()
const { user, logout } = useAuth()

const toast = ref({ show: false, msg: '', type: '' })
let toastTimer
function showToast(msg, type = '') {
  toast.value = { show: true, msg, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2600)
}

// ===== 执政官信息 =====
const admin = ref(null)

// 后台顶栏头像：优先展示头像图，加载失败回退首字母
const adminAvatar = computed(() => admin.value?.avatar?.trim() || null)
const adminAvatarErr = ref(false)
watch(adminAvatar, () => (adminAvatarErr.value = false))
async function loadProfile() {
  if (user.value) { admin.value = user.value; return }
  try {
    const { data } = await adminProfile()
    if (data?.code === 0 && data?.data) admin.value = data.data
  } catch { /* 401 时拦截器会清理会话并跳后台登录页 */ }
}

function onLogout() {
  logout()
  router.push('/')
}
function goHome() {
  router.push('/home')
}

// ===== 模块切换 =====
const tab = ref('archive')   // 'archive' 档案管理 / 'users' 用户管理

// ===== 用户管理 =====
const uKeyword = ref('')
const uPermission = ref('')   // '' 全部 / 0 管理员 / 1 默认
const uStatus = ref('')       // '' 全部 / 0 正常 / 1 封禁
const uPage = ref(1)
const uSize = 10
const users = ref([])
const uTotal = ref(0)
const uLoading = ref(false)
const confirmUserId = ref(null)

const uTotalPages = computed(() => Math.max(1, Math.ceil(uTotal.value / uSize)))

async function fetchUsers() {
  uLoading.value = true
  try {
    const { data } = await adminUserList({
      keyword: uKeyword.value.trim() || undefined,
      permission: uPermission.value === '' ? undefined : Number(uPermission.value),
      status: uStatus.value === '' ? undefined : Number(uStatus.value),
      page: uPage.value,
      size: uSize
    })
    if (data?.code === 0 && data?.data) {
      users.value = data.data.records || []
      uTotal.value = data.data.total || 0
    } else {
      users.value = []
      uTotal.value = 0
    }
  } catch {
    users.value = []
    uTotal.value = 0
  } finally {
    uLoading.value = false
  }
}

let uSearchTimer
watch([uKeyword, uPermission, uStatus], () => {
  clearTimeout(uSearchTimer)
  uPage.value = 1
  uSearchTimer = setTimeout(fetchUsers, 300)
})
watch(uPage, fetchUsers)

function permText(p) {
  return p === 0 ? '管理员' : '默认'
}
function statusTextU(s) {
  return s === 1 ? '封禁' : '正常'
}

async function toggleUserStatus(it) {
  const next = it.status === 1 ? 0 : 1
  try {
    const { data } = await adminUserSetStatus(it.id, next)
    if (data?.code === 0) {
      Object.assign(it, data.data || { status: next })
      showToast(next === 1 ? `已封禁「${it.username}」。` : `已解封「${it.username}」。`, 'ok')
    } else {
      showToast(data?.msg || '操作失败。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，操作失败。', 'err')
  }
}

async function toggleUserPerm(it) {
  // 当前管理员(0) -> 降为默认(1)；默认(1) -> 提升为管理员(0)
  const next = it.permission === 0 ? 1 : 0
  try {
    const { data } = await adminUserSetPermission(it.id, next)
    if (data?.code === 0) {
      Object.assign(it, data.data || { permission: next })
      showToast(next === 0 ? `已提升「${it.username}」为执政官。` : `已收回「${it.username}」执政官权限。`, 'ok')
    } else {
      showToast(data?.msg || '操作失败。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，操作失败。', 'err')
  }
}

function askDeleteUser(it) {
  confirmUserId.value = it.id
}
function cancelDeleteUser() {
  confirmUserId.value = null
}
async function doDeleteUser(it) {
  confirmUserId.value = null
  try {
    const { data } = await adminUserDelete(it.id)
    if (data?.code === 0) {
      showToast(`守夜人「${it.username}」已从名册抹去。`, 'ok')
      fetchUsers()
    } else {
      showToast(data?.msg || '删除失败。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，删除失败。', 'err')
  }
}

// ===== 档案批量上下架 =====
const batchIds = ref([])
const batchStatus = ref(1)
async function doBatchStatus() {
  if (!batchIds.value.length) return showToast('请先勾选档案。', 'err')
  try {
    const { data } = await archiveBatchStatus([...batchIds.value], batchStatus.value)
    if (data?.code === 0) {
      showToast(data.msg || `已批量${batchStatus.value === 1 ? '上架' : '下架'} ${batchIds.value.length} 条档案。`, 'ok')
      batchIds.value = []
      fetchList()
    } else {
      showToast(data?.msg || '批量操作失败。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，批量操作失败。', 'err')
  }
}

onMounted(() => {
  loadProfile()
  fetchList()
})

// 切入用户管理标签时拉取用户列表（首次）
watch(tab, (t) => {
  if (t === 'users' && !users.value.length && !uLoading.value) {
    fetchUsers()
  }
})

// ===== 档案列表 =====
const query = ref('')
const type = ref('')
const status = ref('')   // '' 全部 / 1 上架 / 0 下架
const page = ref(1)
const size = 10
const list = ref([])
const total = ref(0)
const loading = ref(false)

const types = ['禁墟', '神秘', '守夜人', '任务']
const typeGlyph = { 禁墟: '✦', 神秘: '☽', 守夜人: '⚔', 任务: '◈' }

async function fetchList() {
  loading.value = true
  try {
    const { data } = await archiveAdminList({
      keyword: query.value.trim() || undefined,
      type: type.value || undefined,
      status: status.value === '' ? undefined : Number(status.value),
      page: page.value,
      size
    })
    if (data?.code === 0 && data?.data) {
      list.value = data.data.records || []
      total.value = data.data.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

let searchTimer
watch([query, type, status], () => {
  clearTimeout(searchTimer)
  page.value = 1
  searchTimer = setTimeout(fetchList, 300)
})
watch(page, fetchList)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

function statusText(s) {
  return s === 1 ? '上架' : '下架'
}
function fmtTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

// ===== 新增 / 修改弹窗 =====
const editing = ref(false)      // true=修改，false=新增
const modal = ref(null)         // null 关闭；否则为表单对象
const saving = ref(false)

const emptyForm = { name: '', type: '禁墟', realm: '', description: '', cover: '', tags: '', status: 1, sort: 0 }
const form = ref({ ...emptyForm })
const coverErr = ref(false)   // 封面预览加载失败标记

// 标签输入 -> 芯片列表预览（按中英文逗号/顿号/空格分隔）
function parseTagList(s) {
  return String(s || '').split(/[,，、\s]+/).map(t => t.trim()).filter(Boolean).slice(0, 8)
}

function openCreate() {
  editing.value = false
  form.value = { ...emptyForm }
  coverErr.value = false
  modal.value = { ...form.value }
}
function openEdit(it) {
  editing.value = true
  form.value = {
    id: it.id,
    name: it.name || '',
    type: it.type || '禁墟',
    realm: it.realm || '',
    description: it.desc || '',
    cover: it.cover || '',
    tags: typeof it.tags === 'string' ? it.tags : (Array.isArray(it.tags) ? it.tags.join(',') : ''),
    status: it.status ?? 1,
    sort: it.sort ?? 0
  }
  coverErr.value = false
  modal.value = { ...form.value }
}
function closeModal() {
  modal.value = null
  saving.value = false
}

async function save() {
  if (saving.value) return
  const f = form.value
  if (!f.name.trim()) return showToast('请填写档案名称。', 'err')
  if (!f.description.trim()) return showToast('请填写档案描述。', 'err')
  const coverUrl = f.cover.trim()
  if (coverUrl && !/^(https?:\/\/|data:image\/)/i.test(coverUrl)) {
    return showToast('封面地址需以 http:// 或 https:// 开头。', 'err')
  }

  saving.value = true
  const payload = {
    name: f.name.trim(),
    type: f.type,
    realm: f.realm.trim() || undefined,
    description: f.description.trim(),
    cover: coverUrl || undefined,
    tags: parseTagList(f.tags).join(',') || undefined,
    status: Number(f.status),
    sort: Number(f.sort) || 0
  }
  try {
    const { data } = editing.value
      ? await archiveUpdate(f.id, payload)
      : await archiveCreate(payload)
    if (data?.code === 0) {
      showToast(editing.value ? `档案「${payload.name}」已更新。` : `档案「${payload.name}」已录入神域。`, 'ok')
      closeModal()
      fetchList()
    } else {
      showToast(data?.msg || '保存失败，请稍后再试。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，保存失败。', 'err')
  } finally {
    saving.value = false
  }
}

// ===== 删除（两步确认） =====
const confirmId = ref(null)
function askDelete(it) {
  confirmId.value = it.id
}
function cancelDelete() {
  confirmId.value = null
}
async function doDelete(it) {
  confirmId.value = null
  try {
    const { data } = await archiveDelete(it.id)
    if (data?.code === 0) {
      showToast(`档案「${it.name}」已从神域抹去。`, 'ok')
      fetchList()
    } else {
      showToast(data?.msg || '删除失败，请稍后再试。', 'err')
    }
  } catch (e) {
    showToast(e.response?.data?.msg || '神域动荡，删除失败。', 'err')
  }
}

onMounted(() => {
  loadProfile()
  fetchList()
})
</script>

<template>
  <main class="admin">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="brand">
        <span class="sigil">⚙</span>
        <span class="brand-txt">神域后台 · 执政官</span>
      </div>

      <div class="user">
        <span class="avatar">
          <img v-if="adminAvatar && !adminAvatarErr" :src="adminAvatar" alt="avatar" @error="adminAvatarErr = true" />
          <template v-else>{{ (admin?.nickname || admin?.username || '执')[0] }}</template>
        </span>
        <span class="uname">执政官「<b>{{ admin?.nickname || admin?.username || '…' }}</b>」</span>
        <button class="ghost-btn" @click="goHome">返回神域</button>
        <button class="logout" @click="onLogout">退出后台</button>
      </div>
    </nav>

    <!-- 模块切换 -->
    <div class="tabs">
      <button :class="{ on: tab === 'archive' }" @click="tab = 'archive'">📜 档案管理</button>
      <button :class="{ on: tab === 'users' }" @click="tab = 'users'">👥 用户管理</button>
    </div>

    <!-- 档案工具栏 -->
    <section class="toolbar" v-if="tab === 'archive'">
      <div class="searchbar">
        <svg class="s-ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7" /><path d="M21 21l-4.3-4.3" />
        </svg>
        <input v-model="query" type="text" placeholder="按名称检索档案…" />
      </div>

      <select v-model="type" class="sel">
        <option value="">全部类型</option>
        <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
      </select>

      <select v-model="status" class="sel">
        <option value="">上架+下架</option>
        <option value="1">仅上架</option>
        <option value="0">仅下架</option>
      </select>

      <button class="create" @click="openCreate">＋ 新增档案</button>
    </section>

    <!-- 批量上下架 -->
    <section class="batchbar" v-if="tab === 'archive'">
      <span class="dim">已选 {{ batchIds.length }} 条</span>
      <select v-model="batchStatus" class="sel">
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button class="ghost-btn" @click="doBatchStatus">批量应用</button>
    </section>

    <!-- 档案表格 -->
    <section class="table-wrap" v-if="tab === 'archive'">
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:46px">选</th>
            <th style="width:52px">ID</th>
            <th>名称</th>
            <th style="width:86px">类型</th>
            <th style="width:110px">境界</th>
            <th>描述</th>
            <th style="width:64px">战力</th>
            <th style="width:76px">状态</th>
            <th style="width:130px">创建时间</th>
            <th style="width:150px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="center dim">检索中…</td>
          </tr>
          <tr v-else-if="!list.length">
            <td colspan="10" class="center dim">神域档案空空如也，点击右上角「新增档案」录入第一条记录。</td>
          </tr>
          <tr v-else v-for="it in list" :key="it.id">
            <td>
              <input type="checkbox" class="cbx" :value="it.id" v-model="batchIds" />
            </td>
            <td class="dim">#{{ it.id }}</td>
            <td class="strong">
              <span class="glyph" :data-type="it.type">{{ typeGlyph[it.type] || '◈' }}</span>
              {{ it.name }}
            </td>
            <td><span class="tag" :data-type="it.type">{{ it.type }}</span></td>
            <td class="dim">{{ it.realm || '—' }}</td>
            <td class="desc" :title="it.desc">{{ it.desc }}</td>
            <td class="dim">{{ it.sort ?? 0 }}</td>
            <td>
              <span class="st" :data-s="it.status">{{ statusText(it.status) }}</span>
            </td>
            <td class="dim">{{ fmtTime(it.createTime) }}</td>
            <td>
              <button class="op edit" @click="openEdit(it)">编辑</button>
              <template v-if="confirmId === it.id">
                <button class="op sure" @click="doDelete(it)">确认抹去</button>
                <button class="op" @click="cancelDelete">取消</button>
              </template>
              <button v-else class="op del" @click="askDelete(it)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pager" v-if="totalPages > 1">
        <button :disabled="page <= 1" @click="page--">‹ 上一页</button>
        <span class="dim">{{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
        <button :disabled="page >= totalPages" @click="page++">下一页 ›</button>
      </div>
    </section>

    <!-- 用户管理 -->
    <section class="usertool" v-if="tab === 'users'">
      <div class="searchbar">
        <svg class="s-ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7" /><path d="M21 21l-4.3-4.3" />
        </svg>
        <input v-model="uKeyword" type="text" placeholder="按账号 / 昵称 / 邮箱检索…" />
      </div>
      <select v-model="uPermission" class="sel">
        <option value="">全部权限</option>
        <option value="0">管理员</option>
        <option value="1">默认</option>
      </select>
      <select v-model="uStatus" class="sel">
        <option value="">全部状态</option>
        <option value="0">正常</option>
        <option value="1">封禁</option>
      </select>
    </section>

    <section class="table-wrap" v-if="tab === 'users'">
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:52px">ID</th>
            <th>账号</th>
            <th>昵称</th>
            <th style="width:110px">权限</th>
            <th style="width:76px">状态</th>
            <th style="width:130px">创建时间</th>
            <th style="width:230px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="uLoading">
            <td colspan="7" class="center dim">检索中…</td>
          </tr>
          <tr v-else-if="!users.length">
            <td colspan="7" class="center dim">神域尚无其他守夜人，前去首页注册一位吧。</td>
          </tr>
          <tr v-else v-for="it in users" :key="it.id">
            <td class="dim">#{{ it.id }}</td>
            <td class="strong">{{ it.username }}</td>
            <td class="dim">{{ it.nickname || '—' }}</td>
            <td>
              <span class="ptag" :data-p="it.permission">{{ permText(it.permission) }}</span>
            </td>
            <td>
              <span class="st" :data-s="it.status">{{ statusTextU(it.status) }}</span>
            </td>
            <td class="dim">{{ fmtTime(it.createTime) }}</td>
            <td>
              <button class="op edit" @click="toggleUserStatus(it)">
                {{ it.status === 1 ? '解封' : '封禁' }}
              </button>
              <button class="op" :class="it.permission === 0 ? 'down' : 'up'" @click="toggleUserPerm(it)">
                {{ it.permission === 0 ? '降为默认' : '提升管理员' }}
              </button>
              <template v-if="confirmUserId === it.id">
                <button class="op sure" @click="doDeleteUser(it)">确认抹去</button>
                <button class="op" @click="cancelDeleteUser">取消</button>
              </template>
              <button v-else class="op del" @click="askDeleteUser(it)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pager" v-if="uTotalPages > 1">
        <button :disabled="uPage <= 1" @click="uPage--">‹ 上一页</button>
        <span class="dim">{{ uPage }} / {{ uTotalPages }} 页 · 共 {{ uTotal }} 位</span>
        <button :disabled="uPage >= uTotalPages" @click="uPage++">下一页 ›</button>
      </div>
    </section>

    <transition name="toast">
      <div class="toast" :data-type="toast.type" v-if="toast.show">{{ toast.msg }}</div>
    </transition>

    <!-- 新增/修改弹窗 -->
    <transition name="modal">
      <div v-if="modal" class="modal-mask" @click.self="closeModal">
        <form class="modal" @submit.prevent="save">
          <button class="modal-close" type="button" @click="closeModal" aria-label="关闭">✕</button>
          <h3>{{ editing ? '修改档案' : '录入新档案' }}</h3>

          <label>档案名称 *</label>
          <input v-model="form.name" type="text" placeholder="如：灯塔囚笼" />

          <div class="row2">
            <div>
              <label>类型</label>
              <select v-model="form.type" class="sel full">
                <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>
            <div>
              <label>境界</label>
              <input v-model="form.realm" type="text" placeholder="如：盏境" />
            </div>
          </div>

          <label>档案描述 *</label>
          <textarea v-model="form.description" rows="4" placeholder="描述这处禁墟 / 人物 / 任务的传说……"></textarea>

          <label>封面图 URL（可选）</label>
          <input v-model="form.cover" type="text" placeholder="https://… 用于卡片展示封面" />
          <div class="cover-preview" v-if="form.cover.trim()">
            <div v-if="coverErr" class="cover-err">⚠ 封面图片加载失败，请检查链接是否有效</div>
            <img
              v-else
              :src="form.cover.trim()"
              alt="封面预览"
              @error="coverErr = true"
              @load="coverErr = false"
            />
          </div>

          <label>标签（可选，逗号分隔）</label>
          <input v-model="form.tags" type="text" placeholder="如：高危, 须组队, 禁物" />
          <div class="tag-preview" v-if="parseTagList(form.tags).length">
            <span v-for="tg in parseTagList(form.tags)" :key="tg" :data-type="form.type">{{ tg }}</span>
          </div>

          <div class="row2">
            <div>
              <label>状态</label>
              <select v-model="form.status" class="sel full">
                <option :value="1">上架</option>
                <option :value="0">下架</option>
              </select>
            </div>
            <div>
              <label>战力排序（0-999）</label>
              <input v-model.number="form.sort" type="number" min="0" max="999" />
            </div>
          </div>

          <div class="actions">
            <button type="button" class="ghost-btn" @click="closeModal">取消</button>
            <button type="submit" class="create" :disabled="saving">
              {{ saving ? '保 存 中 …' : (editing ? '保存修改' : '录入神域') }}
            </button>
          </div>
        </form>
      </div>
    </transition>
  </main>
</template>

<style scoped>
.admin {
  position: relative; z-index: 2;
  min-height: 100vh; overflow-y: auto;
  padding: 0 clamp(20px, 4vw, 48px) 60px;
}
.admin::-webkit-scrollbar { width: 8px; }
.admin::-webkit-scrollbar-thumb { background: rgba(120,150,255,.25); border-radius: 8px; }
.admin::-webkit-scrollbar-track { background: transparent; }

/* ===== 导航栏 ===== */
.navbar {
  position: sticky; top: 0; z-index: 5;
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  padding: 14px 0;
  background: linear-gradient(180deg, rgba(4,5,13,.85), rgba(4,5,13,.45) 70%, transparent);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--glass-bd);
}
.brand { display: flex; align-items: center; gap: 11px; font-weight: 700; letter-spacing: .12em; white-space: nowrap; }
.sigil {
  width: 34px; height: 34px; display: grid; place-items: center;
  border-radius: 9px; background: linear-gradient(135deg, var(--gold), var(--pink));
  color: #04050d; font-weight: 800; box-shadow: 0 0 18px rgba(245,210,122,.4);
}
.brand-txt { background: linear-gradient(100deg, var(--gold), var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent; }
.user { display: flex; align-items: center; gap: 10px; white-space: nowrap; font-size: 13.5px; color: var(--ink-dim); }
.avatar {
  width: 32px; height: 32px; border-radius: 50%; display: grid; place-items: center;
  background: linear-gradient(135deg, var(--gold), var(--pink)); color: #04050d; font-weight: 800;
  overflow: hidden;
}
.avatar img {
  width: 100%; height: 100%; border-radius: 50%; object-fit: cover; display: block;
}
.user b { color: var(--gold); }
.ghost-btn {
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03);
  color: var(--ink); padding: 8px 14px; border-radius: 9px; cursor: pointer;
  font-family: inherit; font-size: 13px; transition: .2s;
}
.ghost-btn:hover { border-color: var(--cyan); color: #fff; }
.logout:hover { border-color: var(--pink); color: #fff; box-shadow: 0 0 16px rgba(255,77,141,.3); }

/* ===== 工具栏 ===== */
.toolbar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  margin: 24px 0 16px;
}
.searchbar {
  flex: 1; min-width: 220px; display: flex; align-items: center; gap: 10px;
  padding: 0 14px; border-radius: 12px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(14px);
  transition: .2s;
}
.searchbar:focus-within { border-color: rgba(245,210,122,.5); box-shadow: 0 0 18px rgba(245,210,122,.15); }
.s-ic { width: 18px; height: 18px; color: var(--gold); flex-shrink: 0; }
.searchbar input {
  flex: 1; border: 0; background: transparent; outline: none;
  color: #fff; font-size: 14px; font-family: inherit; padding: 11px 2px;
}
.searchbar input::placeholder { color: #5a6390; }
.sel {
  padding: 10px 12px; border-radius: 12px; font-family: inherit; font-size: 13.5px;
  color: var(--ink); background: rgba(8,10,24,.6); border: 1px solid var(--glass-bd);
  cursor: pointer; outline: none; transition: .2s;
}
.sel:hover, .sel:focus { border-color: rgba(120,150,255,.4); }
.sel.full { width: 100%; }
.create {
  margin-left: auto;
  border: 0; border-radius: 12px; padding: 11px 20px; cursor: pointer;
  font-family: inherit; font-size: 14px; font-weight: 700; letter-spacing: .08em; color: #04050d;
  background: linear-gradient(100deg, var(--gold), #ffb46b 55%, var(--pink));
  box-shadow: 0 4px 22px rgba(245,210,122,.3); transition: .25s;
}
.create:hover:not(:disabled) { filter: brightness(1.08); transform: translateY(-1px); }
.create:disabled { opacity: .6; cursor: wait; }

/* ===== 表格 ===== */
.table-wrap {
  border-radius: 16px; overflow: auto;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(14px);
  box-shadow: 0 14px 44px rgba(0,0,0,.4);
}
.tbl { width: 100%; border-collapse: collapse; min-width: 880px; }
.tbl th {
  text-align: left; font-size: 12.5px; letter-spacing: .1em; color: var(--ink-dim);
  padding: 13px 14px; border-bottom: 1px solid var(--glass-bd);
  position: sticky; top: 0; background: rgba(8,10,24,.85); backdrop-filter: blur(8px); z-index: 1;
}
.tbl td {
  padding: 12px 14px; font-size: 13.5px; color: var(--ink);
  border-bottom: 1px solid rgba(120,150,255,.07);
  vertical-align: middle;
}
.tbl tbody tr { transition: background .2s; }
.tbl tbody tr:hover { background: rgba(120,150,255,.06); }
.tbl tbody tr:last-child td { border-bottom: 0; }
.strong { color: #fff; font-weight: 600; }
.desc {
  max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  color: var(--ink-dim);
}
.center { text-align: center; padding: 34px 0; }
.dim { color: var(--ink-dim); }
.glyph {
  display: inline-grid; place-items: center; width: 26px; height: 26px;
  border-radius: 8px; font-size: 13px; margin-right: 6px; vertical-align: middle;
  background: linear-gradient(135deg, rgba(46,230,255,.2), rgba(177,77,255,.2));
  color: var(--cyan);
}
.glyph[data-type="禁墟"] { color: var(--violet); }
.glyph[data-type="守夜人"] { color: var(--gold); }
.glyph[data-type="任务"] { color: var(--pink); }
.tag {
  display: inline-block; font-size: 11.5px; letter-spacing: .08em;
  padding: 2px 10px; border-radius: 999px; color: var(--violet);
  border: 1px solid rgba(177,77,255,.3); background: rgba(177,77,255,.08);
}
.tag[data-type="神秘"] { color: var(--cyan); border-color: rgba(46,230,255,.3); background: rgba(46,230,255,.08); }
.tag[data-type="守夜人"] { color: var(--gold); border-color: rgba(245,210,122,.3); background: rgba(245,210,122,.08); }
.tag[data-type="任务"] { color: var(--pink); border-color: rgba(255,77,141,.3); background: rgba(255,77,141,.08); }
.st {
  display: inline-block; font-size: 11.5px; padding: 3px 10px; border-radius: 999px;
  letter-spacing: .06em;
}
.st[data-s="1"] { color: #7dffb0; border: 1px solid rgba(125,255,176,.35); background: rgba(125,255,176,.08); }
.st[data-s="0"] { color: #ff9ec4; border: 1px solid rgba(255,158,196,.35); background: rgba(255,158,196,.08); }
.op {
  border: 1px solid var(--glass-bd); background: transparent; color: var(--ink-dim);
  padding: 5px 10px; border-radius: 8px; cursor: pointer; font-family: inherit; font-size: 12.5px;
  margin-right: 4px; transition: .2s;
}
.op.edit:hover { color: var(--cyan); border-color: var(--cyan); }
.op.del:hover { color: var(--pink); border-color: var(--pink); }
.op.sure { color: var(--pink); border-color: var(--pink); font-weight: 700; }

.pager {
  display: flex; align-items: center; justify-content: center; gap: 16px;
  padding: 14px; font-size: 13px;
}
.pager button {
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink);
  padding: 7px 14px; border-radius: 9px; cursor: pointer; font-family: inherit; font-size: 13px;
  transition: .2s;
}
.pager button:hover:not(:disabled) { border-color: var(--cyan); color: #fff; }
.pager button:disabled { opacity: .35; cursor: not-allowed; }

/* ===== 模块切换 ===== */
.tabs { display: flex; gap: 8px; margin: 18px 0 4px; flex-wrap: wrap; }
.tabs button {
  border: 1px solid var(--glass-bd); background: rgba(8,10,24,.4);
  color: var(--ink-dim); padding: 9px 18px; border-radius: 11px; cursor: pointer;
  font-family: inherit; font-size: 14px; letter-spacing: .08em; transition: .2s;
}
.tabs button:hover { color: #fff; border-color: rgba(120,150,255,.4); }
.tabs button.on {
  color: #fff; border-color: rgba(245,210,122,.4);
  background: linear-gradient(100deg, rgba(245,210,122,.16), rgba(255,77,141,.14));
  box-shadow: 0 0 16px rgba(245,210,122,.22);
}

/* ===== 批量上下架 ===== */
.batchbar { display: flex; align-items: center; gap: 12px; margin: 14px 0 6px; }
.batchbar .dim { font-size: 13px; }

/* 选择框 */
.cbx { width: 16px; height: 16px; accent-color: var(--cyan); cursor: pointer; }

/* ===== 用户管理 ===== */
.usertool {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin: 18px 0 6px;
}
.ptag {
  display: inline-block; font-size: 11.5px; padding: 3px 10px; border-radius: 999px; letter-spacing: .06em;
}
.ptag[data-p="0"] { color: var(--gold); border: 1px solid rgba(245,210,122,.35); background: rgba(245,210,122,.08); }
.ptag[data-p="1"] { color: var(--cyan); border: 1px solid rgba(46,230,255,.3); background: rgba(46,230,255,.07); }
.op.up:hover { color: var(--gold); border-color: var(--gold); }
.op.down:hover { color: var(--violet); border-color: var(--violet); }

/* ===== Toast ===== */
.toast {
  position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%);
  background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff;
  padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20;
  box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px);
  white-space: nowrap; max-width: 90vw; overflow: hidden; text-overflow: ellipsis;
}
.toast[data-type='err'] { border-color: rgba(255,77,141,.5); }
.toast[data-type='ok'] { border-color: rgba(245,210,122,.5); }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }

/* ===== 弹窗 ===== */
.modal-mask {
  position: fixed; inset: 0; z-index: 30;
  display: grid; place-items: center; padding: 20px;
  background: rgba(2,3,9,.66); backdrop-filter: blur(6px);
}
.modal {
  position: relative; width: min(520px, 100%);
  max-height: 88vh; overflow-y: auto;
  padding: 28px 28px 24px; border-radius: 20px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(24px) saturate(140%);
  box-shadow: 0 30px 90px rgba(0,0,0,.6), inset 0 1px 0 rgba(255,255,255,.06);
  animation: pop .3s cubic-bezier(.22,.61,.36,1) both;
}
@keyframes pop { from { opacity: 0; transform: translateY(18px) scale(.95); } to { opacity: 1; transform: none; } }
.modal h3 {
  margin-bottom: 18px; font-size: 19px; letter-spacing: .08em; color: #fff;
  background: linear-gradient(100deg, #fff, var(--gold) 70%);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.modal label { display: block; margin: 12px 0 6px; font-size: 12.5px; color: var(--ink-dim); letter-spacing: .06em; }
.modal input, .modal textarea {
  width: 100%; box-sizing: border-box;
  padding: 10px 12px; border-radius: 10px;
  background: rgba(8,10,24,.5); border: 1px solid var(--glass-bd);
  color: #fff; font-size: 14px; font-family: inherit; outline: none; transition: .2s;
  resize: vertical;
}
.modal input:focus, .modal textarea:focus {
  border-color: rgba(245,210,122,.5);
  box-shadow: 0 0 0 3px rgba(245,210,122,.08);
}
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }

/* 封面实时预览 */
.cover-preview { margin-top: 8px; }
.cover-preview img {
  display: block; width: 100%; height: 150px; object-fit: cover;
  border-radius: 10px; border: 1px solid var(--glass-bd);
}
.cover-err {
  padding: 12px; border-radius: 10px; font-size: 12.5px; color: #ff9ec4;
  border: 1px dashed rgba(255,158,196,.4); background: rgba(255,77,141,.06);
}

/* 标签实时预览 */
.tag-preview { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.tag-preview span {
  font-size: 11.5px; letter-spacing: .08em; padding: 3px 11px; border-radius: 999px;
  color: var(--violet); border: 1px solid rgba(177,77,255,.3); background: rgba(177,77,255,.08);
}
.tag-preview span[data-type="神秘"] { color: var(--cyan); border-color: rgba(46,230,255,.3); background: rgba(46,230,255,.08); }
.tag-preview span[data-type="守夜人"] { color: var(--gold); border-color: rgba(245,210,122,.3); background: rgba(245,210,122,.08); }
.tag-preview span[data-type="任务"] { color: var(--pink); border-color: rgba(255,77,141,.3); background: rgba(255,77,141,.08); }
.modal-close {
  position: absolute; top: 14px; right: 14px; width: 30px; height: 30px;
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink-dim);
  border-radius: 9px; cursor: pointer; font-size: 14px; transition: .2s; line-height: 1;
}
.modal-close:hover { color: #fff; border-color: var(--pink); }
.modal-enter-active, .modal-leave-active { transition: .25s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

@media (max-width: 760px) {
  .user .uname { display: none; }
  .create { margin-left: 0; }
}
</style>
