<template>
  <div class="space-y-10">
    <header
      class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"
    >
      <div class="space-y-3">
        <h1 class="font-serif text-4xl font-medium text-ink">
          {{ localeStore.t.aiHeaderTitle }}
        </h1>
        <p class="text-ink-light">
          {{ localeStore.t.aiHeaderSubtitle }}
        </p>
        <p class="text-ink-light">
          {{ localeStore.t.aiHeaderNote }}
        </p>
      </div>
      <Button
        variant="outline"
        size="sm"
        class="self-start rounded-full border-ink/20 text-ink"
        :disabled="!hasResults"
        @click="openResultPanel()"
      >
        {{ localeStore.t.aiViewResults }}
      </Button>
    </header>

    <!-- 连接设置 -->
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <CardTitle class="font-serif text-2xl text-ink">
          {{ localeStore.t.aiConnectionSettings }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-6">
        <div class="space-y-4">
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">Base URL</label>
              <Input
                v-model="aiBaseUrl"
                placeholder="https://api.openai.com"
                autocomplete="off"
              />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiModelId }}
              </label>
              <Input
                v-model="aiModel"
                placeholder="gpt-4o-mini"
                autocomplete="off"
              />
            </div>
            <div class="space-y-2 md:col-span-2">
              <label class="text-xs font-medium text-ink-light">API Key</label>
              <Input
                v-model="aiApiKey"
                type="password"
                placeholder="sk-..."
                autocomplete="off"
              />
              <label
                class="mt-2 flex items-center gap-2 text-xs text-ink-light"
              >
                <input
                  v-model="rememberKey"
                  type="checkbox"
                  class="h-4 w-4 rounded border-ink/20 accent-clay"
                />
                {{ localeStore.t.aiRememberKey }}
              </label>
            </div>
          </div>
          <p class="text-xs text-ink-light">
            {{ localeStore.t.aiBaseUrlHint }}
          </p>
          <div class="flex flex-wrap gap-2">
            <Button
              variant="outline"
              size="sm"
              class="rounded-full border-ink/20 text-ink"
              @click="clearOverrides"
            >
              {{ localeStore.t.aiReset }}
            </Button>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">
            {{ localeStore.t.aiChatSettings }}
          </h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-4">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiProvider }}
              </label>
              <select
                v-model="chatProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">{{ localeStore.t.aiDefault }}</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiTemperature }}
              </label>
              <Input
                v-model.number="chatTemperature"
                type="number"
                step="0.1"
                min="0"
                max="2"
              />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiMaxTokens }}
              </label>
              <Input
                v-model.number="chatMaxTokens"
                type="number"
                min="64"
                max="4096"
              />
            </div>
            <label class="mt-7 flex items-center gap-2 text-sm text-ink-light">
              <input
                v-model="useChatStream"
                type="checkbox"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              {{ localeStore.t.aiStreamLogin }}
            </label>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">
            {{ localeStore.t.aiContentSettings }}
          </h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiProvider }}
              </label>
              <select
                v-model="assistantProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">{{ localeStore.t.aiDefault }}</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
          </div>
        </div>

        <div class="border-t border-ink/10 pt-4 space-y-4">
          <h3 class="text-sm font-semibold text-ink">
            {{ localeStore.t.aiWritingSettings }}
          </h3>
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiProvider }}
              </label>
              <select
                v-model="writingProvider"
                class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
              >
                <option value="">{{ localeStore.t.aiDefault }}</option>
                <option value="openai">OpenAI</option>
                <option value="anthropic">Anthropic</option>
              </select>
            </div>
            <label class="mt-7 flex items-center gap-2 text-sm text-ink-light">
              <input
                v-model="useWritingStream"
                type="checkbox"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              {{ localeStore.t.aiStreamLogin }}
            </label>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- AI 对话 -->
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <CardTitle class="font-serif text-2xl text-ink">
            {{ localeStore.t.aiChatTitle }}
          </CardTitle>
          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              class="rounded-full border-ink/20 text-ink"
              @click="resetChat"
            >
              {{ localeStore.t.aiClearChat }}
            </Button>
            <Button
              v-if="isChatting && useChatStream"
              variant="ghost"
              size="sm"
              class="rounded-full text-clay"
              @click="stopChatStream"
            >
              {{ localeStore.t.aiStopOutput }}
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent class="space-y-4">
        <div
          ref="chatContainer"
          class="max-h-[420px] space-y-3 overflow-y-auto rounded-2xl border border-ink/10 bg-paper-dark/30 p-4"
        >
          <div v-if="chatMessages.length === 0" class="text-sm text-ink-light">
            {{ localeStore.t.aiChatEmpty }}
          </div>
          <div
            v-for="(message, index) in chatMessages"
            :key="`${message.role}-${index}`"
            class="flex"
            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-relaxed"
              :class="
                message.role === 'user'
                  ? 'bg-ink text-white'
                  : 'bg-paper-card text-ink border border-ink/10'
              "
            >
              <p v-if="message.role === 'user'" class="whitespace-pre-wrap">
                {{ message.content }}
              </p>
              <MarkdownRenderer
                v-else
                :content="message.content"
                class="prose-sm text-sm leading-relaxed"
              />
            </div>
          </div>
        </div>
        <div class="flex flex-col gap-3 md:flex-row">
          <Textarea
            v-model="chatInput"
            :placeholder="localeStore.t.aiChatPlaceholder"
            class="min-h-20 flex-1"
            @keydown.enter.exact.prevent="sendChat"
          />
          <Button
            class="h-11 rounded-xl bg-ink px-6 text-paper-bg hover:bg-clay dark:bg-clay dark:text-paper-bg"
            :disabled="isChatting"
            @click="sendChat"
          >
            {{ isChatting ? localeStore.t.aiProcessing : localeStore.t.aiSend }}
          </Button>
        </div>
        <p
          v-if="useChatStream && !userStore.isLoggedIn"
          class="text-xs text-clay"
        >
          {{ localeStore.t.aiChatStreamHint }}
        </p>
      </CardContent>
    </Card>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <!-- 内容助手 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            {{ localeStore.t.aiContentAssistantTitle }}
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-5">
          <Textarea
            v-model="assistantContent"
            :placeholder="localeStore.t.aiContentPlaceholder"
            class="min-h-32"
          />
          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiSummaryLength }}
              </label>
              <Input
                v-model.number="assistantMaxLength"
                type="number"
                min="50"
                max="500"
              />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-medium text-ink-light">
                {{ localeStore.t.aiTitleTagCount }}
              </label>
              <Input
                v-model.number="assistantCount"
                type="number"
                min="1"
                max="10"
              />
            </div>
          </div>
          <div class="flex flex-wrap gap-2">
            <Button
              class="rounded-full bg-ink text-white hover:bg-clay"
              :disabled="assistantLoading.summary"
              @click="handleSummary"
            >
              {{
                assistantLoading.summary
                  ? localeStore.t.aiGenerating
                  : localeStore.t.aiGenerateSummary
              }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="assistantLoading.titles"
              @click="handleTitles"
            >
              {{
                assistantLoading.titles
                  ? localeStore.t.aiGenerating
                  : localeStore.t.aiSuggestTitles
              }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="assistantLoading.tags"
              @click="handleTags"
            >
              {{
                assistantLoading.tags
                  ? localeStore.t.aiGenerating
                  : localeStore.t.aiSuggestTags
              }}
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- 写作助手 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            {{ localeStore.t.aiWritingAssistantTitle }}
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-5">
          <div class="space-y-2">
            <label class="text-xs font-medium text-ink-light">
              {{ localeStore.t.aiTopicLabel }}
            </label>
            <Input
              v-model="outlineTopic"
              :placeholder="localeStore.t.aiTopicPlaceholder"
            />
          </div>
          <Button
            variant="outline"
            class="rounded-full border-ink/20 text-ink"
            :disabled="writingLoading.outline"
            @click="handleOutline"
          >
            {{
              writingLoading.outline
                ? localeStore.t.aiGenerating
                : localeStore.t.aiGenerateOutline
            }}
          </Button>
          <Textarea
            v-model="writingContent"
            :placeholder="localeStore.t.aiWritingPlaceholder"
            class="min-h-32"
          />
          <div class="flex flex-wrap gap-2">
            <Button
              class="rounded-full bg-ink text-white hover:bg-clay"
              :disabled="writingLoading.continue"
              @click="handleContinue"
            >
              {{
                writingLoading.continue
                  ? localeStore.t.aiProcessing
                  : localeStore.t.aiContinueArticle
              }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="writingLoading.polish"
              @click="handlePolish"
            >
              {{
                writingLoading.polish
                  ? localeStore.t.aiProcessing
                  : localeStore.t.aiPolish
              }}
            </Button>
            <Button
              variant="outline"
              class="rounded-full border-ink/20 text-ink"
              :disabled="writingLoading.translate"
              @click="handleTranslate"
            >
              {{
                writingLoading.translate
                  ? localeStore.t.aiProcessing
                  : localeStore.t.aiTranslate
              }}
            </Button>
            <Button
              v-if="writingLoading.continue && useWritingStream"
              variant="ghost"
              class="rounded-full text-clay"
              @click="stopWritingStream"
            >
              {{ localeStore.t.aiStopContinue }}
            </Button>
          </div>
          <div class="space-y-2">
            <label class="text-xs font-medium text-ink-light">
              {{ localeStore.t.aiTargetLanguage }}
            </label>
            <Input
              v-model="targetLanguage"
              :placeholder="localeStore.t.aiTargetPlaceholder"
            />
          </div>
          <p
            v-if="useWritingStream && !userStore.isLoggedIn"
            class="text-xs text-clay"
          >
            {{ localeStore.t.aiWritingStreamHint }}
          </p>
        </CardContent>
      </Card>
    </div>

    <Dialog :open="isResultPanelOpen" @update:open="setResultPanelOpen">
      <DialogContent
        class="sketch-dialog w-[92vw] max-w-3xl rounded-2xl border-ink/10 bg-paper-card p-0 shadow-soft"
      >
        <DialogHeader class="border-b border-ink/10 px-6 py-4 pr-16 sm:pr-20">
          <div class="flex items-start justify-between gap-3">
            <div class="space-y-1">
              <DialogTitle class="font-serif text-2xl text-ink">
                {{ localeStore.t.aiResultTitle }}
              </DialogTitle>
              <DialogDescription class="text-xs text-ink-light">
                {{ localeStore.t.aiResultSubtitle }}
              </DialogDescription>
            </div>
            <div class="flex flex-nowrap items-center gap-2 whitespace-nowrap">
              <Button
                variant="outline"
                size="sm"
                class="h-8 rounded-full border-ink/20 px-3 text-xs text-ink"
                @click="
                  resultSections.summary = false;
                  resultSections.titles = false;
                  resultSections.tags = false;
                  resultSections.outline = false;
                  resultSections.continuation = false;
                  resultSections.polish = false;
                  resultSections.translate = false;
                "
              >
                {{ localeStore.t.aiCollapseAll }}
              </Button>
              <Button
                variant="outline"
                size="sm"
                class="h-8 rounded-full border-ink/20 px-3 text-xs text-ink"
                @click="
                  resultSections.summary = true;
                  resultSections.titles = true;
                  resultSections.tags = true;
                  resultSections.outline = true;
                  resultSections.continuation = true;
                  resultSections.polish = true;
                  resultSections.translate = true;
                "
              >
                {{ localeStore.t.aiExpandAll }}
              </Button>
              <Button
                variant="outline"
                size="sm"
                class="h-8 rounded-full border-ink/20 px-3 text-xs text-ink"
                :disabled="!hasResults"
                @click="clearResults"
              >
                {{ localeStore.t.aiClearResults }}
              </Button>
            </div>
          </div>
        </DialogHeader>
        <div
          ref="resultBodyRef"
          class="result-body space-y-3 overflow-y-auto px-6 py-4"
        >
          <div v-if="!hasResults" class="text-sm text-ink-light">
            {{ localeStore.t.aiNoResults }}
          </div>

          <Collapsible
            v-if="assistantSummary"
            :open="resultSections.summary"
            @update:open="(value) => (resultSections.summary = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionSummary }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.summary ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="resultSections.summary ? 'sketch-open' : 'sketch-closed'"
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(assistantSummary)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3 text-sm text-ink-light whitespace-pre-wrap"
                >
                  {{ assistantSummary }}
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="assistantTitles.length"
            :open="resultSections.titles"
            @update:open="(value) => (resultSections.titles = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionTitles }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.titles ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="resultSections.titles ? 'sketch-open' : 'sketch-closed'"
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(assistantTitles)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <div class="flex flex-wrap gap-1.5">
                    <span
                      v-for="title in assistantTitles"
                      :key="title"
                      class="rounded-full border border-ink/10 bg-paper-dark/60 px-2.5 py-0.5 text-xs text-ink"
                    >
                      {{ title }}
                    </span>
                  </div>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="assistantTags.length"
            :open="resultSections.tags"
            @update:open="(value) => (resultSections.tags = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionTags }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.tags ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="resultSections.tags ? 'sketch-open' : 'sketch-closed'"
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(assistantTags)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <div class="flex flex-wrap gap-1.5">
                    <span
                      v-for="tag in assistantTags"
                      :key="tag"
                      class="rounded-full border border-ink/10 bg-paper-card px-2.5 py-0.5 text-xs text-ink-light"
                    >
                      # {{ tag }}
                    </span>
                  </div>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="outline.length"
            :open="resultSections.outline"
            @update:open="(value) => (resultSections.outline = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionOutline }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.outline ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="resultSections.outline ? 'sketch-open' : 'sketch-closed'"
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(outline)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <ul class="list-disc pl-5 text-sm text-ink-light">
                    <li v-for="item in outline" :key="item">{{ item }}</li>
                  </ul>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="writingLoading.continue || continueOutput"
            :open="resultSections.continuation"
            @update:open="(value) => (resultSections.continuation = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionContinuation }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.continuation ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="
                resultSections.continuation ? 'sketch-open' : 'sketch-closed'
              "
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    v-if="continueOutput"
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(continueOutput)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <MarkdownRenderer
                    v-if="continueOutput"
                    :content="continueOutput"
                    class="prose-sm"
                  />
                  <p v-else class="text-sm text-ink-light">
                    {{ localeStore.t.aiContinuationGenerating }}
                  </p>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="polishOutput"
            :open="resultSections.polish"
            @update:open="(value) => (resultSections.polish = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionPolish }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.polish ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="resultSections.polish ? 'sketch-open' : 'sketch-closed'"
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(polishOutput)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <MarkdownRenderer :content="polishOutput" class="prose-sm" />
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible
            v-if="translateOutput"
            :open="resultSections.translate"
            @update:open="(value) => (resultSections.translate = value)"
          >
            <CollapsibleTrigger
              class="flex w-full items-center justify-between rounded-xl border border-ink/10 bg-paper-dark/30 px-3 py-2 text-sm text-ink"
            >
              <span>{{ localeStore.t.aiSectionTranslate }}</span>
              <ChevronDown
                class="size-4 transition-transform duration-300"
                :class="resultSections.translate ? 'rotate-180' : ''"
              />
            </CollapsibleTrigger>
            <CollapsibleContent
              :force-mount="true"
              class="sketch-collapse"
              :class="
                resultSections.translate ? 'sketch-open' : 'sketch-closed'
              "
            >
              <div class="sketch-collapse-inner">
                <div class="flex justify-end">
                  <button
                    type="button"
                    class="text-xs text-clay transition-colors hover:text-ink"
                    @click="appendToWritingContent(translateOutput)"
                  >
                    {{ localeStore.t.aiInsertToWriting }}
                  </button>
                </div>
                <div
                  class="mt-2 rounded-xl border border-ink/10 bg-paper-card p-3"
                >
                  <MarkdownRenderer
                    :content="translateOutput"
                    class="prose-sm"
                  />
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>
        </div>
      </DialogContent>
    </Dialog>

    <div
      v-if="isResultMinimized && hasResults"
      class="fixed bottom-6 right-6 z-50"
    >
      <Button
        variant="outline"
        class="h-10 rounded-full border-ink/20 bg-paper-card px-4 text-sm text-ink shadow-soft"
        @click="openResultPanel()"
      >
        {{ localeStore.t.aiResultMinimized }}
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { toast } from "vue-sonner";
import { aiApi } from "@/api/ai";
import type { AiMessage, AiChatRequest } from "@/types/api";
import { useUserStore } from "@/stores/user";
import { useLocaleStore } from "@/stores/locale";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import { ChevronDown } from "lucide-vue-next";

type ResultSectionKey =
  | "summary"
  | "titles"
  | "tags"
  | "outline"
  | "continuation"
  | "polish"
  | "translate";

const userStore = useUserStore();
const localeStore = useLocaleStore();

const storageKeys = {
  baseUrl: "ai.baseUrl",
  model: "ai.model",
  apiKey: "ai.apiKey",
  rememberKey: "ai.rememberKey",
  chatHistory: "ai.chatHistory",
};

const chatHistoryLimit = 60;

/**
 * 从本地存储恢复对话历史。
 */
function loadChatHistory(): AiMessage[] {
  const raw = localStorage.getItem(storageKeys.chatHistory);
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed
      .filter(
        (item) =>
          item &&
          typeof item.content === "string" &&
          ["system", "user", "assistant"].includes(item.role),
      )
      .slice(-chatHistoryLimit);
  } catch (error) {
    return [];
  }
}

/**
 * 持久化对话历史。
 */
function persistChatHistory(messages: AiMessage[]) {
  const trimmed = messages.slice(-chatHistoryLimit);
  if (trimmed.length === 0) {
    localStorage.removeItem(storageKeys.chatHistory);
    return;
  }
  localStorage.setItem(storageKeys.chatHistory, JSON.stringify(trimmed));
}

const aiBaseUrl = ref(localStorage.getItem(storageKeys.baseUrl) || "");
const aiModel = ref(localStorage.getItem(storageKeys.model) || "");
const storedRememberKey =
  localStorage.getItem(storageKeys.rememberKey) === "true";
const rememberKey = ref(storedRememberKey);
const aiApiKey = ref(
  storedRememberKey
    ? localStorage.getItem(storageKeys.apiKey) || ""
    : sessionStorage.getItem(storageKeys.apiKey) || "",
);

const chatMessages = ref<AiMessage[]>(loadChatHistory());
const chatInput = ref("");
const chatProvider = ref("");
const chatTemperature = ref(0.7);
const chatMaxTokens = ref(4096);
const useChatStream = ref(true);
const isChatting = ref(false);
const chatController = ref<AbortController | null>(null);
const chatContainer = ref<HTMLElement | null>(null);

const assistantContent = ref("");
const assistantSummary = ref("");
const assistantTitles = ref<string[]>([]);
const assistantTags = ref<string[]>([]);
const assistantMaxLength = ref(200);
const assistantCount = ref(3);
const assistantProvider = ref("");
const assistantLoading = ref({
  summary: false,
  titles: false,
  tags: false,
});

const outlineTopic = ref("");
const outline = ref<string[]>([]);
const writingContent = ref("");
const continueOutput = ref("");
const polishOutput = ref("");
const translateOutput = ref("");
const targetLanguage = ref(localeStore.locale === "zh-CN" ? "英文" : "English");
const writingProvider = ref("");
const useWritingStream = ref(true);
const writingController = ref<AbortController | null>(null);
const writingLoading = ref({
  outline: false,
  continue: false,
  polish: false,
  translate: false,
});

const isResultPanelOpen = ref(false);
const isResultMinimized = ref(false);
const resultBodyRef = ref<HTMLElement | null>(null);
const resultSections = reactive<Record<ResultSectionKey, boolean>>({
  summary: true,
  titles: true,
  tags: true,
  outline: true,
  continuation: true,
  polish: true,
  translate: true,
});
const hasResults = computed(
  () =>
    Boolean(assistantSummary.value) ||
    assistantTitles.value.length > 0 ||
    assistantTags.value.length > 0 ||
    outline.value.length > 0 ||
    Boolean(continueOutput.value) ||
    Boolean(polishOutput.value) ||
    Boolean(translateOutput.value),
);

/**
 * 监听聊天记录变化并持久化。
 */
watch(
  chatMessages,
  (value) => {
    persistChatHistory(value);
  },
  { deep: true },
);

watch(
  () => chatMessages.value.length,
  () => {
    scrollChatToBottom();
  },
);

function animateResultBodyHeight() {
  const el = resultBodyRef.value;
  if (!el) return;
  const currentHeight = el.getBoundingClientRect().height;
  el.style.height = `${currentHeight}px`;
  requestAnimationFrame(() => {
    const maxHeight = Math.floor(window.innerHeight * 0.7);
    const targetHeight = Math.min(el.scrollHeight, maxHeight);
    el.style.height = `${targetHeight}px`;
  });
}

/**
 * 滚动聊天窗口到底部。
 */
function scrollChatToBottom() {
  nextTick(() => {
    if (!chatContainer.value) return;
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  });
}

watch(aiBaseUrl, (value) => {
  if (value) {
    localStorage.setItem(storageKeys.baseUrl, value);
  } else {
    localStorage.removeItem(storageKeys.baseUrl);
  }
});

watch(aiModel, (value) => {
  if (value) {
    localStorage.setItem(storageKeys.model, value);
  } else {
    localStorage.removeItem(storageKeys.model);
  }
});

watch(rememberKey, (value) => {
  localStorage.setItem(storageKeys.rememberKey, String(value));
  if (value) {
    sessionStorage.removeItem(storageKeys.apiKey);
    if (aiApiKey.value) {
      localStorage.setItem(storageKeys.apiKey, aiApiKey.value);
    } else {
      localStorage.removeItem(storageKeys.apiKey);
    }
    return;
  }
  localStorage.removeItem(storageKeys.apiKey);
  if (aiApiKey.value) {
    sessionStorage.setItem(storageKeys.apiKey, aiApiKey.value);
  } else {
    sessionStorage.removeItem(storageKeys.apiKey);
  }
});

watch(aiApiKey, (value) => {
  const storage = rememberKey.value ? localStorage : sessionStorage;
  if (value) {
    storage.setItem(storageKeys.apiKey, value);
  } else {
    storage.removeItem(storageKeys.apiKey);
  }
});

watch(
  isResultPanelOpen,
  async (open) => {
    if (!open) return;
    await nextTick();
    animateResultBodyHeight();
  },
  { flush: "post" },
);

watch(
  resultSections,
  async () => {
    await nextTick();
    animateResultBodyHeight();
  },
  { deep: true, flush: "post" },
);

watch(
  [
    assistantSummary,
    assistantTitles,
    assistantTags,
    outline,
    continueOutput,
    polishOutput,
    translateOutput,
  ],
  async () => {
    await nextTick();
    animateResultBodyHeight();
  },
  { deep: true, flush: "post" },
);

/**
 * 控制结果面板的显示状态。
 */
function setResultPanelOpen(value: boolean) {
  isResultPanelOpen.value = value;
  if (value) {
    isResultMinimized.value = false;
    return;
  }
  if (hasResults.value) {
    isResultMinimized.value = true;
  }
}

/**
 * 打开结果面板并展开指定分区。
 */
function openResultPanel(section?: ResultSectionKey) {
  isResultPanelOpen.value = true;
  isResultMinimized.value = false;
  if (section) {
    resultSections[section] = true;
  }
}

/**
 * 清空已生成的结果内容。
 */
function clearResults() {
  assistantSummary.value = "";
  assistantTitles.value = [];
  assistantTags.value = [];
  outline.value = [];
  continueOutput.value = "";
  polishOutput.value = "";
  translateOutput.value = "";
  isResultPanelOpen.value = false;
  isResultMinimized.value = false;
}

/**
 * 将生成结果插入到写作输入区。
 */
function appendToWritingContent(content: string | string[]) {
  const text = Array.isArray(content) ? content.join("\n") : content;
  if (!text || !text.trim()) return;
  const separator = writingContent.value.trim() ? "\n\n" : "";
  writingContent.value = `${writingContent.value}${separator}${text.trim()}`;
  toast.success(localeStore.t.aiInsertSuccess);
}

/**
 * 清空对话记录。
 */
function resetChat() {
  chatMessages.value = [];
  chatInput.value = "";
  persistChatHistory([]);
  stopChatStream();
}

function stopChatStream() {
  if (chatController.value) {
    chatController.value.abort();
    chatController.value = null;
  }
  isChatting.value = false;
}

function clearOverrides() {
  aiBaseUrl.value = "";
  aiModel.value = "";
  aiApiKey.value = "";
  rememberKey.value = false;
}

function buildOverrides() {
  const baseUrl = aiBaseUrl.value.trim();
  const model = aiModel.value.trim();
  const apiKey = aiApiKey.value.trim();

  if (baseUrl && !/^https?:\/\//i.test(baseUrl)) {
    toast.error(localeStore.t.aiErrorBaseUrlInvalid);
    return null;
  }
  if (baseUrl && !apiKey) {
    toast.error(localeStore.t.aiErrorBaseUrlNeedKey);
    return null;
  }

  return {
    baseUrl: baseUrl || undefined,
    model: model || undefined,
    apiKey: apiKey || undefined,
  };
}

async function sendChat() {
  const content = chatInput.value.trim();
  if (!content || isChatting.value) return;

  const overrides = buildOverrides();
  if (!overrides) return;

  const nextMessages: AiMessage[] = [
    ...chatMessages.value,
    { role: "user", content },
  ];
  chatMessages.value = nextMessages;
  chatInput.value = "";

  const payload: AiChatRequest = {
    messages: nextMessages,
    temperature: Number.isFinite(chatTemperature.value)
      ? chatTemperature.value
      : undefined,
    maxTokens: Number.isFinite(chatMaxTokens.value)
      ? chatMaxTokens.value
      : undefined,
    provider: chatProvider.value || undefined,
    ...overrides,
  };

  if (useChatStream.value && !userStore.isLoggedIn) {
    useChatStream.value = false;
  }

  if (useChatStream.value) {
    chatMessages.value = [
      ...chatMessages.value,
      { role: "assistant", content: "" },
    ];
    const assistantIndex = chatMessages.value.length - 1;
    isChatting.value = true;
    chatController.value = aiApi.chatStream(payload, {
      onMessage: (chunk) => {
        const currentMessage = chatMessages.value[assistantIndex];
        if (currentMessage) {
          currentMessage.content += chunk;
        } else {
          chatMessages.value.push({ role: "assistant", content: chunk });
        }
        scrollChatToBottom();
      },
      onDone: () => {
        isChatting.value = false;
        chatController.value = null;
        scrollChatToBottom();
      },
      onError: (message) => {
        toast.error(localeStore.t.aiErrorChatStreamFailed, {
          description: message,
        });
        isChatting.value = false;
        chatController.value = null;
      },
    });
    return;
  }

  isChatting.value = true;
  try {
    const response = await aiApi.chat(payload);
    chatMessages.value = [
      ...chatMessages.value,
      { role: "assistant", content: response },
    ];
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorChatFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    isChatting.value = false;
  }
}

async function handleSummary() {
  if (!assistantContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.summary = true;
  try {
    assistantSummary.value = await aiApi.generateSummary({
      content: assistantContent.value,
      maxLength: assistantMaxLength.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("summary");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorSummaryFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    assistantLoading.value.summary = false;
  }
}

async function handleTitles() {
  if (!assistantContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.titles = true;
  try {
    assistantTitles.value = await aiApi.suggestTitles({
      content: assistantContent.value,
      count: assistantCount.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("titles");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorTitlesFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    assistantLoading.value.titles = false;
  }
}

async function handleTags() {
  if (!assistantContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  assistantLoading.value.tags = true;
  try {
    assistantTags.value = await aiApi.suggestTags({
      content: assistantContent.value,
      count: assistantCount.value || undefined,
      provider: assistantProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("tags");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorTagsFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    assistantLoading.value.tags = false;
  }
}

async function handleOutline() {
  if (!outlineTopic.value.trim()) {
    toast.error(localeStore.t.aiErrorTopicRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.outline = true;
  try {
    outline.value = await aiApi.generateOutline({
      topic: outlineTopic.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("outline");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorOutlineFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    writingLoading.value.outline = false;
  }
}

async function handleContinue() {
  if (!writingContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;

  if (useWritingStream.value && !userStore.isLoggedIn) {
    useWritingStream.value = false;
  }

  if (useWritingStream.value) {
    continueOutput.value = "";
    writingLoading.value.continue = true;
    openResultPanel("continuation");
    writingController.value = aiApi.continueWritingStream(
      {
        content: writingContent.value,
        provider: writingProvider.value || undefined,
        ...overrides,
      },
      {
        onMessage: (chunk) => {
          continueOutput.value += chunk;
        },
        onDone: () => {
          writingLoading.value.continue = false;
          writingController.value = null;
        },
        onError: (message) => {
          toast.error(localeStore.t.aiErrorContinueFailed, {
            description: message,
          });
          writingLoading.value.continue = false;
          writingController.value = null;
        },
      },
    );
    return;
  }

  writingLoading.value.continue = true;
  try {
    continueOutput.value = await aiApi.continueWriting({
      content: writingContent.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("continuation");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorContinueFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    writingLoading.value.continue = false;
  }
}

function stopWritingStream() {
  if (writingController.value) {
    writingController.value.abort();
    writingController.value = null;
  }
  writingLoading.value.continue = false;
}

onBeforeUnmount(() => {
  stopChatStream();
  stopWritingStream();
  window.removeEventListener("resize", animateResultBodyHeight);
});

onMounted(() => {
  window.addEventListener("resize", animateResultBodyHeight);
  nextTick(() => animateResultBodyHeight());
});

async function handlePolish() {
  if (!writingContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.polish = true;
  try {
    polishOutput.value = await aiApi.polish({
      content: writingContent.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("polish");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorPolishFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    writingLoading.value.polish = false;
  }
}

async function handleTranslate() {
  if (!writingContent.value.trim()) {
    toast.error(localeStore.t.aiErrorContentRequired);
    return;
  }
  if (!targetLanguage.value.trim()) {
    toast.error(localeStore.t.aiErrorTargetLanguageRequired);
    return;
  }
  const overrides = buildOverrides();
  if (!overrides) return;
  writingLoading.value.translate = true;
  try {
    translateOutput.value = await aiApi.translate({
      content: writingContent.value,
      targetLanguage: targetLanguage.value,
      provider: writingProvider.value || undefined,
      ...overrides,
    });
    openResultPanel("translate");
  } catch (err: any) {
    toast.error(localeStore.t.aiErrorTranslateFailed, {
      description: err.message || localeStore.t.aiErrorGenericRetry,
    });
  } finally {
    writingLoading.value.translate = false;
  }
}
</script>

<style scoped>
@keyframes sketch-pop {
  0% {
    opacity: 0;
    transform: translateY(10px) scale(0.98) rotate(-0.35deg);
  }
  60% {
    opacity: 1;
    transform: translateY(-2px) scale(1.01) rotate(0.2deg);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0deg);
  }
}

@keyframes sketch-out {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0deg);
  }
  100% {
    opacity: 0;
    transform: translateY(12px) scale(0.98) rotate(0.4deg);
  }
}

.sketch-dialog[data-state="open"] {
  animation: sketch-pop 420ms cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: 50% 20%;
}

.sketch-dialog[data-state="closed"] {
  animation: sketch-out 240ms ease-in;
}

.result-body {
  height: 70vh;
  max-height: 70vh;
  transition: height 360ms cubic-bezier(0.22, 1, 0.36, 1);
}

/* 手绘风折叠动画 - 使用 grid 技巧实现稳定的高度过渡 */
.sketch-collapse {
  display: grid;
  overflow: hidden;
  transform-origin: top center;
  transition:
    grid-template-rows 520ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 260ms ease-out,
    transform 520ms cubic-bezier(0.22, 1, 0.36, 1);
  will-change: grid-template-rows, opacity, transform;
}

/* 内层容器用于隐藏溢出内容 */
.sketch-collapse-inner {
  overflow: hidden;
  opacity: 0;
  transform: translateY(-4px);
  transition:
    opacity 360ms ease,
    transform 480ms cubic-bezier(0.22, 1, 0.36, 1);
}

/* 收起状态 */
.sketch-collapse.sketch-closed {
  grid-template-rows: 0fr;
  opacity: 0;
  transform: translateY(-6px) rotate(-0.25deg);
}

/* 展开状态 */
.sketch-collapse.sketch-open {
  grid-template-rows: 1fr;
  opacity: 1;
  transform: translateY(0) rotate(0deg);
}

.sketch-collapse.sketch-open .sketch-collapse-inner {
  opacity: 1;
  transform: translateY(0);
}

:global(html) {
  scrollbar-gutter: stable;
}

:global(body) {
  scrollbar-gutter: stable;
  overflow-y: scroll;
}
</style>
