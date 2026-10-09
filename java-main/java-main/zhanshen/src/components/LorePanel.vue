<script setup>
const tags = [
  { label: '禁墟', value: '凡尘神域' },
  { label: '神位', value: '炽天使' },
  { label: '阵营', value: '夜幕小队' },
  { label: '境界', value: '克莱因' }
]
</script>

<template>
  <section class="lore">
    <svg class="crest" viewBox="0 0 100 100" aria-hidden="true">
      <g class="ring r1" fill="none" stroke="#2ee6ff" stroke-width="1.4" opacity="0.9">
        <circle cx="50" cy="50" r="46" stroke-dasharray="2 6" />
        <circle cx="50" cy="50" r="38" stroke="#b14dff" stroke-dasharray="10 8" opacity="0.8" />
      </g>
      <g class="ring r2" fill="none" stroke="#f5d27a">
        <circle cx="50" cy="50" r="30" stroke-width="1" stroke-dasharray="1 4" opacity="0.7" />
        <path d="M50 14 L50 22 M50 78 L50 86 M14 50 L22 50 M78 50 L86 50" stroke-width="2" />
      </g>
      <text x="50" y="60" text-anchor="middle" font-size="34" font-weight="800"
            fill="#fff" font-family="serif">神</text>
    </svg>

    <div class="kicker">Zhɑn Shen · Mortal Realm</div>
    <h1 class="title">
      斩神之凡尘神域
      <span class="sub">守 夜 人 认 证 终 端</span>
    </h1>

    <p class="oath">
      若黯夜终临，吾必立于万万人前，<br />
      <b>横刀向渊，血染天穹。</b><br />
      迷雾止步于大夏之境，凡尘之中，亦可弑神。
    </p>

    <div class="tags">
      <span class="tag" v-for="t in tags" :key="t.label">
        {{ t.label }} · <span>{{ t.value }}</span>
      </span>
    </div>
  </section>
</template>

<style scoped>
.lore { max-width: 560px; }

.crest {
  width: 92px; height: 92px; margin-bottom: 26px;
  filter: drop-shadow(0 0 16px rgba(46, 230, 255, .5));
}
.crest .ring { transform-origin: 50% 50%; }
.crest .r1 { animation: spin 22s linear infinite; }
.crest .r2 { animation: spin 16s linear infinite reverse; }
@keyframes spin { to { transform: rotate(360deg); } }

.kicker {
  letter-spacing: .42em; font-size: 13px; color: var(--cyan);
  text-transform: uppercase; margin-bottom: 14px;
  text-shadow: 0 0 14px rgba(46, 230, 255, .5);
}

h1.title {
  font-size: clamp(40px, 5.4vw, 70px);
  line-height: 1.04; font-weight: 800; letter-spacing: .02em;
  background: linear-gradient(100deg, #fff 0%, var(--cyan) 30%, var(--violet) 62%, var(--pink) 100%);
  -webkit-background-clip: text; background-clip: text; color: transparent;
  filter: drop-shadow(0 4px 30px rgba(177, 77, 255, .35));
  margin-bottom: 8px;
}
h1.title .sub {
  display: block; font-size: .42em; letter-spacing: .5em; color: var(--gold);
  -webkit-text-fill-color: var(--gold); margin-top: 14px;
  text-shadow: 0 0 18px rgba(245, 210, 122, .4);
}

.oath {
  margin: 30px 0 26px; padding-left: 18px;
  border-left: 2px solid var(--violet);
  font-size: 16px; line-height: 2; color: var(--ink);
  font-style: italic;
}
.oath b { color: var(--gold); font-style: normal; font-weight: 700; }

.tags { display: flex; flex-wrap: wrap; gap: 10px; }
.tag {
  font-size: 12.5px; letter-spacing: .12em; color: var(--ink-dim);
  border: 1px solid var(--glass-bd); border-radius: 999px;
  padding: 7px 14px; background: rgba(255, 255, 255, .02);
  backdrop-filter: blur(4px);
}
.tag span { color: var(--cyan); }

@media (max-width: 880px) {
  .lore { margin: 0 auto; text-align: center; }
  .oath { border-left: 0; padding-left: 0; }
  .tags { justify-content: center; }
  .crest { margin: 0 auto 22px; }
  h1.title .sub { letter-spacing: .3em; }
}
</style>
