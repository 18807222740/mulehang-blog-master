<template>
  <div
    ref="containerRef"
    class="markdown-body prose prose-lg max-w-none"
    v-html="renderedHtml"
  ></div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from "vue";
import MarkdownIt from "markdown-it";
import { full as markdownItEmoji } from "markdown-it-emoji";
import markdownItKatex from "markdown-it-katex";
import hljs from "highlight.js";
import mermaid from "mermaid";
import "highlight.js/styles/github.css";
import "katex/dist/katex.min.css";

/**
 * 组件 Props
 */
const props = defineProps<{
  content: string;
}>();

const containerRef = ref<HTMLElement | null>(null);

/**
 * 配置 Markdown-it 实例
 */
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
});

/**
 * 为块级 token 注入行号数据，便于编辑/预览定位同步。
 */
const lineTrackedTokens = [
  "paragraph_open",
  "heading_open",
  "blockquote_open",
  "bullet_list_open",
  "ordered_list_open",
  "list_item_open",
  "table_open",
  "thead_open",
  "tbody_open",
  "tr_open",
  "th_open",
  "td_open",
  "hr",
];

const getLineAttrs = (token: any) => {
  if (!token?.map) return "";
  const [start, end] = token.map;
  if (typeof start !== "number" || typeof end !== "number") return "";
  return ` data-line-start="${start}" data-line-end="${end}"`;
};

lineTrackedTokens.forEach((ruleName) => {
  const defaultRender =
    md.renderer.rules[ruleName] ||
    ((tokens, idx, options, env, self) => {
      void env;
      return self.renderToken(tokens, idx, options);
    });
  md.renderer.rules[ruleName] = (tokens, idx, options, env, self) => {
    const token = tokens[idx];
    if (token?.map) {
      token.attrSet("data-line-start", String(token.map[0]));
      token.attrSet("data-line-end", String(token.map[1]));
    }
    return defaultRender(tokens, idx, options, env, self);
  };
});

md.use(markdownItEmoji as never);

const katexPlugin =
  (markdownItKatex as unknown as { default?: unknown })?.default ??
  markdownItKatex;
if (katexPlugin) {
  md.use(katexPlugin as never);
}

const mermaidApi =
  (mermaid as unknown as { default?: typeof mermaid })?.default ?? mermaid;

md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  void options;
  void env;
  void self;
  const token = tokens[idx];
  if (!token) return "";
  const lang = (token.info || "").trim().toLowerCase();
  const lineAttrs = getLineAttrs(token);
  if (lang === "mermaid") {
    const code = md.utils.escapeHtml(token.content);
    return `<div class="mermaid"${lineAttrs}>${code}</div>`;
  }
  if (lang && hljs.getLanguage(lang)) {
    try {
      const highlighted = hljs.highlight(token.content, {
        language: lang,
      }).value;
      return `<pre class="hljs"${lineAttrs}><code>${highlighted}</code></pre>`;
    } catch (error) {
      console.error("代码高亮失败:", error);
    }
  }
  const escaped = md.utils.escapeHtml(token.content);
  return `<pre class="hljs"${lineAttrs}><code>${escaped}</code></pre>`;
};

md.renderer.rules.code_block = (tokens, idx) => {
  const token = tokens[idx];
  if (!token) return "";
  const lineAttrs = getLineAttrs(token);
  const escaped = md.utils.escapeHtml(token.content);
  return `<pre class="hljs"${lineAttrs}><code>${escaped}</code></pre>`;
};

md.options.highlight = (str: string, lang?: string): string => {
  if (lang && hljs.getLanguage(lang)) {
    try {
      return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`;
    } catch (error) {
      console.error("代码高亮失败:", error);
    }
  }
  return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`;
};

/**
 * 渲染 Markdown 内容
 */
const renderedHtml = computed(() => {
  if (!props.content) return "";
  return md.render(props.content);
});

/**
 * 获取 Mermaid 主题（跟随暗色模式）
 */
const resolveMermaidTheme = () => {
  const isDark = document.documentElement.classList.contains("dark");
  return isDark ? "dark" : "neutral";
};

/**
 * 执行 Mermaid 渲染
 */
const renderMermaid = async () => {
  if (!containerRef.value) return;
  const nodes = containerRef.value.querySelectorAll<HTMLElement>(".mermaid");
  if (!nodes.length) return;
  nodes.forEach((node) => node.removeAttribute("data-processed"));
  try {
    await mermaidApi.run({ nodes: Array.from(nodes) });
  } catch (error) {
    console.error("Mermaid 渲染失败:", error);
  }
};

/**
 * 重新初始化 Mermaid（主题/配置变更时调用）
 */
const reinitializeMermaid = async () => {
  mermaidApi.initialize({
    startOnLoad: false,
    theme: resolveMermaidTheme(),
    securityLevel: "loose",
  });
  await renderMermaid();
};

let themeObserver: MutationObserver | null = null;

onMounted(async () => {
  await nextTick();
  await reinitializeMermaid();
  themeObserver = new MutationObserver(() => {
    reinitializeMermaid();
  });
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["class"],
  });
});

onBeforeUnmount(() => {
  themeObserver?.disconnect();
  themeObserver = null;
});

watch(
  () => renderedHtml.value,
  async () => {
    await nextTick();
    await renderMermaid();
  },
);
</script>
