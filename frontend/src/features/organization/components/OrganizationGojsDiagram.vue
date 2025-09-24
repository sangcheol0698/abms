<template>
  <div
    ref="diagramRef"
    class="h-[640px] w-full overflow-hidden rounded-xl border border-border/60 bg-card shadow-sm"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import * as go from 'gojs';
import type { OrganizationChartNode } from '@/features/organization/models/organization';
import { useTheme } from '@/core/composables';

defineOptions({ name: 'OrganizationGojsDiagram' });

const props = defineProps<{
  nodes: OrganizationChartNode[];
  selectedNodeId?: string;
}>();

const emit = defineEmits<{ (event: 'update:selectedNodeId', value: string): void }>();

const diagramRef = ref<HTMLDivElement | null>(null);
const { resolvedTheme } = useTheme();
const themeColors = computed(() =>
  resolvedTheme.value === 'dark'
    ? {
        background: '#111827',
        card: '#1f2937',
        border: '#334155',
        text: '#f8fafc',
        subtext: '#cbd5f5',
        headcount: '#f8fafc',
        selection: '#38bdf8',
        shadow: '#0f172a',
        accent: [
          '#38bdf8',
          '#f97316',
          '#a855f7',
          '#60a5fa',
          '#5eead4',
          '#fb7185',
          '#bef264',
          '#facc15',
        ],
      }
    : {
        background: '#f8fafc',
        card: '#ffffff',
        border: '#dbeafe',
        text: '#0f172a',
        subtext: '#475569',
        headcount: '#1e293b',
        selection: '#2563eb',
        shadow: '#e2e8f0',
        accent: [
          '#2563eb',
          '#0ea5e9',
          '#f97316',
          '#6366f1',
          '#22c55e',
          '#f43f5e',
          '#facc15',
          '#ec4899',
        ],
      },
);
let diagram: go.Diagram | null = null;
let initialZoomApplied = false;

onMounted(() => {
  if (!diagramRef.value) {
    return;
  }

  diagram = new go.Diagram(diagramRef.value, {
    allowMove: false,
    allowCopy: false,
    allowDelete: false,
    allowHorizontalScroll: true,
    allowVerticalScroll: true,
    initialAutoScale: go.AutoScale.UniformToFill,
    maxSelectionCount: 1,
    validCycle: go.CycleMode.DestinationTree,
    layout: new go.TreeLayout({
      treeStyle: go.TreeStyle.LastParents,
      arrangement: go.TreeArrangement.Horizontal,
      angle: 90,
      layerSpacing: 60,
      alternateAngle: 90,
      alternateLayerSpacing: 60,
      alternateAlignment: go.TreeAlignment.Bus,
      alternateNodeSpacing: 24,
    }),
    'undoManager.isEnabled': false,
  });

  diagram.nodeTemplate = createNodeTemplate();
  // diagram.linkTemplate = createLinkTemplate();

  diagram.addDiagramListener('ObjectSingleClicked', (event: go.DiagramEvent) => {
    const part = event.subject.part;
    if (part instanceof go.Node) {
      const key = part.data?.key as string | undefined;
      if (key) {
        emit('update:selectedNodeId', key);
      }
    }
  });

  applyModel();
});

onUnmounted(() => {
  if (diagram) {
    diagram.div = null;
    diagram = null;
  }
});

watch(
  () => props.nodes,
  () => {
    applyModel();
  },
  { deep: true },
);

watch(
  () => props.selectedNodeId,
  (nextId) => {
    if (!diagram || !nextId) {
      return;
    }
    const node = diagram.findNodeForKey(nextId);
    if (node) {
      diagram.select(node);
      diagram.centerRect(node.actualBounds);
    }
  },
  { immediate: true },
);

function applyModel() {
  if (!diagram) {
    return;
  }

  const { nodeDataArray, linkDataArray } = flattenNodes(props.nodes);
  diagram.model = new go.GraphLinksModel(nodeDataArray, linkDataArray);
  diagram.background = themeColors.value.background;

  if (props.selectedNodeId) {
    const node = diagram.findNodeForKey(props.selectedNodeId);
    if (node) {
      diagram.select(node);
      diagram.centerRect(node.actualBounds);
    }
  } else if (nodeDataArray.length) {
    const rootKey = nodeDataArray[0].key;
    emit('update:selectedNodeId', rootKey);
  }

  if (!initialZoomApplied) {
    diagram.zoomToFit();
    diagram.scale = Math.max(diagram.scale * 0.7, 0.35);
    diagram.centerRect(diagram.documentBounds);
    initialZoomApplied = true;
  }
}

function flattenNodes(nodes: OrganizationChartNode[]) {
  const nodeDataArray: Array<go.ObjectData> = [];
  const linkDataArray: Array<go.ObjectData> = [];

  function walk(node: OrganizationChartNode, parentKey?: string, depth = 0) {
    nodeDataArray.push({
      key: node.departmentId,
      name: node.departmentName,
      leader: node.departmentLeader?.employeeName ?? '리더 미지정',
      headcount: node.employeeCount ?? 0,
      depth,
      colorIndex: depth,
      dept: node.departmentCode,
    });

    if (parentKey) {
      linkDataArray.push({ from: parentKey, to: node.departmentId });
    }

    node.children.forEach((child) => walk(child, node.departmentId, depth + 1));
  }

  nodes.forEach((root) => walk(root));

  return { nodeDataArray, linkDataArray };
}

function createNodeTemplate(): go.Node {
  const $ = go.GraphObject.make;
  const colors = themeColors.value;
  const accent = colors.accent;

  return $(
    go.Node,
    go.Panel.Spot,
    {
      isShadowed: true,
      shadowOffset: new go.Point(0, 3),
      cursor: 'pointer',
      selectionAdornmentTemplate: $(
        go.Adornment,
        go.Panel.Auto,
        $(go.Shape, 'RoundedRectangle', {
          fill: null,
          stroke: colors.selection,
          strokeWidth: 3,
        }),
        $(go.Placeholder),
      ),
    },
    $(
      go.Panel,
      go.Panel.Auto,
      { name: 'BODY' },
      $(go.Shape, 'RoundedRectangle', {
        name: 'SHAPE',
        fill: resolvedTheme.value === 'dark' ? '#1f2937' : '#f8fafc',
        stroke: resolvedTheme.value === 'dark' ? '#334155' : '#c7d2fe',
        strokeWidth: 1.2,
        spot1: new go.Spot(0, 0, 16, 16),
        spot2: new go.Spot(1, 1, -16, -16),
      }),
      $(
        go.Panel,
        go.Panel.Table,
        {
          margin: new go.Margin(18, 18, 18, 18),
          defaultAlignment: go.Spot.Left,
          stretch: go.Stretch.Horizontal,
        },
        $(
          go.TextBlock,
          {
            row: 0,
            font: '600 14px "Inter", "Pretendard", sans-serif',
            stroke: colors.text,
            isMultiline: true,
            wrap: go.Wrap.Fit,
            width: 220,
          },
          new go.Binding('text', 'name'),
        ),
        $(
          go.Panel,
          go.Panel.Auto,
          {
            row: 1,
            alignment: go.Spot.Left,
            margin: new go.Margin(8, 0, 0, 0),
          },
          $(go.Shape, 'RoundedRectangle', {
            parameter1: 8,
            fill: resolvedTheme.value === 'dark' ? '#0f172a' : '#f0fdf4',
            stroke: resolvedTheme.value === 'dark' ? '#38bdf833' : '#16a34a33',
            strokeWidth: 1,
          }),
          $(
            go.TextBlock,
            {
              margin: new go.Margin(2, 12, 2, 12),
              font: '500 11px "Inter", "Pretendard", sans-serif',
              stroke: resolvedTheme.value === 'dark' ? '#4ade80' : '#15803d',
              wrap: go.Wrap.Fit,
            },
            new go.Binding('text', 'dept'),
          ),
        ),
        $(
          go.TextBlock,
          {
            row: 2,
            margin: new go.Margin(10, 0, 0, 0),
            font: '500 11px "Inter", "Pretendard", sans-serif',
            stroke: colors.subtext,
            wrap: go.Wrap.Fit,
            width: 220,
          },
          new go.Binding('text', '', (data) => `리더 ${data.leader}`),
        ),
        $(
          go.TextBlock,
          {
            row: 3,
            margin: new go.Margin(6, 0, 0, 0),
            font: '600 11px "Inter", "Pretendard", sans-serif',
            stroke: colors.headcount,
            wrap: go.Wrap.Fit,
            width: 220,
          },
          new go.Binding('text', '', (data) => `구성원 ${data.headcount}명`),
        ),
      ),
    ),
    $(
      go.Shape,
      'RoundedLeftRectangle',
      {
        alignment: go.Spot.Left,
        alignmentFocus: go.Spot.Left,
        stretch: go.Stretch.Vertical,
        width: 6,
        strokeWidth: 0,
      },
      new go.Binding('fill', 'colorIndex', (idx) => accent[idx % accent.length]),
    ),
  );
}

watch(themeColors, () => {
  if (!diagram) {
    return;
  }
  const selectedId = props.selectedNodeId;
  diagram.startTransaction('theme');
  diagram.background = themeColors.value.background;
  diagram.nodeTemplate = createNodeTemplate();
  diagram.rebuildParts();
  diagram.commitTransaction('theme');
  if (selectedId) {
    const node = diagram.findNodeForKey(selectedId);
    if (node) {
      diagram.select(node);
    }
  }
});

</script>

<style scoped></style>
