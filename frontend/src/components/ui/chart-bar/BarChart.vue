<script setup lang="ts" generic="T extends Record<string, any>">
import type { BulletLegendItemInterface } from "@unovis/ts"
import type { Component } from "vue"
import type { BaseChartProps } from "."
import { Axis, GroupedBar, StackedBar } from "@unovis/ts"
import { VisAxis, VisGroupedBar, VisStackedBar, VisXYContainer } from "@unovis/vue"
import { useMounted } from "@vueuse/core"
import { computed, ref } from "vue"
import { cn } from "@/lib/utils"
import { ChartCrosshair, ChartLegend, defaultColors } from '@/components/ui/chart'

type BarChartProps = BaseChartProps<T> & {
  /**
   * Render custom tooltip component.
   */
  customTooltip?: Component
  /**
   * Change the type of the chart
   * @default "grouped"
   */
  type?: "stacked" | "grouped"
  /**
   * Rounded bar corners
   * @default 0
   */
  roundedCorners?: number
}

const props = withDefaults(defineProps<BarChartProps>(), {
  type: "grouped",
  margin: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
  filterOpacity: 0.2,
  roundedCorners: 0,
  xTickTextHideOverlapping: false,
  showXAxis: true,
  showYAxis: true,
  showTooltip: true,
  showLegend: true,
  showGridLine: true,
})
const emits = defineEmits<{
  legendItemClick: [d: BulletLegendItemInterface, i: number]
}>()

type KeyOfT = Extract<keyof T, string>
type Data = typeof props.data[number]

const index = computed(() => props.index as KeyOfT)
const colors = computed(() => props.colors?.length ? props.colors : defaultColors(props.categories.length))
const legendItems = ref<BulletLegendItemInterface[]>(props.categories.map((category, i) => ({
  name: category,
  color: colors.value[i],
  inactive: false,
})))

const isMounted = useMounted()

function handleLegendItemClick(d: BulletLegendItemInterface, i: number) {
  emits("legendItemClick", d, i)
}

const VisBarComponent = computed(() => props.type === "grouped" ? VisGroupedBar : VisStackedBar)
const selectorsBar = computed(() => props.type === "grouped" ? GroupedBar.selectors.bar : StackedBar.selectors.bar)
const axisProps = computed(() => {
  const chartProps = props as BarChartProps

  return {
    xTickValues: chartProps.xTickValues,
    xNumTicks: chartProps.xNumTicks,
    xTickTextHideOverlapping: chartProps.xTickTextHideOverlapping,
    yNumTicks: chartProps.yNumTicks,
  }
})
</script>

<template>
  <div :class="cn('w-full h-[400px] flex flex-col items-end', $attrs.class ?? '')">
    <ChartLegend v-if="showLegend" v-model:items="legendItems" @legend-item-click="handleLegendItemClick" />

    <VisXYContainer
      :data="data"
      :style="{ height: isMounted ? '100%' : 'auto' }"
      :margin="margin"
    >
      <ChartCrosshair v-if="showTooltip" :colors="colors" :items="legendItems" :custom-tooltip="customTooltip" :index="index" />

      <VisBarComponent
        :x="(_d: Data, i: number) => i"
        :y="categories.map(category => (d: Data) => d[category]) "
        :color="colors"
        :rounded-corners="roundedCorners"
        :bar-padding="0.05"
        :attributes="{
          [selectorsBar]: {
            opacity: (_d: Data, i:number) => {
              const pos = i % categories.length
              return legendItems[pos]?.inactive ? filterOpacity : 1
            },
          },
        }"
      />

      <VisAxis
        v-if="showXAxis"
        type="x"
        :tick-format="xFormatter ?? ((v: number) => data[v]?.[index])"
        :tick-values="axisProps.xTickValues"
        :num-ticks="axisProps.xNumTicks"
        :tick-text-hide-overlapping="axisProps.xTickTextHideOverlapping"
        :grid-line="false"
        :tick-line="false"
        tick-text-color="var(--muted-foreground)"
      />
      <VisAxis
        v-if="showYAxis"
        type="y"
        :num-ticks="axisProps.yNumTicks"
        :tick-line="false"
        :tick-format="yFormatter"
        :domain-line="false"
        :grid-line="showGridLine"
        :attributes="{
          [Axis.selectors.grid]: {
            class: 'text-muted',
          },
        }"
        tick-text-color="var(--muted-foreground)"
      />

      <slot />
    </VisXYContainer>
  </div>
</template>
