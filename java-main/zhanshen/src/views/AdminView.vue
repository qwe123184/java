<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'
import { adminProfile } from '../api/auth'
import { archiveAdminList, archiveCreate, archiveUpdate, archiveDelete } from '../api/archive'

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

const emptyForm = { name: '', type: '禁墟', realm: '', description: '', status: 1, sort: 0 }
const form = ref({ ...emptyForm })

function openCreate() {
  editing.value = false
  form.value = { ...emptyForm }
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
    status: it.status ?? 1,
    sort: it.sort ?? 0
  }
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

  saving.value = true
  const payload = {
    name: f.name.trim(),
    type: f.type,
    realm: f.realm.trim() || undefined,
    description: f.description.trim(),
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
        <span class="brand-txt">神域后台 · 档案管理</span>
      </div>

      <div class="user">
        <span class="avatar">{{ (admin?.username || '执')[0] }}</span>
        <span class="uname">执政官「<b>{{ admin?.username || '…' }}</b>」</span>
        <button class="ghost-btn" @click="goHome">返回神域</button>
        <button class="logout" @click="onLogout">退出后台</button>
      </div>
    </nav>

    <!-- 工具栏 -->
    <section class="toolbar">
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

    <!-- 档案表格 -->
    <section class="table-wrap">
      <table class="tbl">
        <thead>
          <tr>
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
            <td colspan="9" class="center dim">检索中…</td>
          </tr>
          <tr v-else-if="!list.length">
            <td colspan="9" class="center dim">神域档案空空如也，点击右上角「新增档案」录入第一条记录。</td>
          </tr>
          <tr v-else v-for="it in list" :key="it.id">
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
