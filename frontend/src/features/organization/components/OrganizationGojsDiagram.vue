<template>
  <div
    ref="diagramRef"
    class="h-[520px] w-full overflow-hidden rounded-xl border border-border/60 bg-card shadow-sm"
  />
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import * as go from 'gojs';
import type { OrganizationChartWithEmployeesNode } from '@/features/organization/models/organization';

defineOptions({ name: 'OrganizationGojsDiagram' });

const props = defineProps<{
  nodes: OrganizationChartWithEmployeesNode[];
  selectedNodeId?: string;
}>();

const emit = defineEmits<{ (event: 'update:selectedNodeId', value: string): void }>();

const diagramRef = ref<HTMLDivElement | null>(null);
let diagram: go.Diagram | null = null;

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
}

function flattenNodes(nodes: OrganizationChartWithEmployeesNode[]) {
  const nodeDataArray: Array<go.ObjectData> = [];
  const linkDataArray: Array<go.ObjectData> = [];

  function walk(node: OrganizationChartWithEmployeesNode, parentKey?: string, depth = 0) {
    nodeDataArray.push({
      key: node.departmentId,
      name: node.departmentName,
      leader: node.departmentLeader?.employeeName ?? '리더 미지정',
      headcount: node.employees.length,
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
  const accent = [
    '#AC193D',
    '#2672EC',
    '#8C0095',
    '#5133AB',
    '#008299',
    '#D24726',
    '#008A00',
    '#094AB2',
  ];

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
          stroke: '#2563eb',
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
        fill: '#ffffff',
        stroke: '#d1d5db',
        strokeWidth: 1.2,
        spot1: new go.Spot(0, 0, 16, 16),
        spot2: new go.Spot(1, 1, -16, -16),
      }),
      $(
        go.Panel,
        go.Panel.Table,
        { margin: new go.Margin(20, 20, 20, 20), defaultAlignment: go.Spot.Left },
        $(
          go.Panel,
          go.Panel.Horizontal,
          { row: 0, alignment: go.Spot.Left, defaultAlignment: go.Spot.Center },
          $(
            go.TextBlock,
            {
              font: '600 14px "Inter", "Pretendard", sans-serif',
              stroke: '#111827',
              isMultiline: true,
            },
            new go.Binding('text', 'name'),
          ),
          $(
            go.Panel,
            go.Panel.Auto,
            { margin: new go.Margin(0, 0, 0, 12) },
            $(go.Shape, 'RoundedRectangle', {
              parameter1: 8,
              fill: '#f0fdf4',
              stroke: '#16a34a33',
              strokeWidth: 1,
            }),
            $(
              go.TextBlock,
              {
                margin: new go.Margin(2, 8, 2, 8),
                font: '500 11px "Inter", "Pretendard", sans-serif',
                stroke: '#15803d',
              },
              new go.Binding('text', 'dept'),
            ),
          ),
        ),
        $(
          go.TextBlock,
          {
            row: 1,
            margin: new go.Margin(8, 0, 0, 0),
            font: '500 11px "Inter", "Pretendard", sans-serif',
            stroke: '#475569',
          },
          new go.Binding('text', '', (data) => `리더 ${data.leader}`),
        ),
        $(
          go.TextBlock,
          {
            row: 2,
            margin: new go.Margin(6, 0, 0, 0),
            font: '600 11px "Inter", "Pretendard", sans-serif',
            stroke: '#1e293b',
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

function createLinkTemplate(): go.Link {
  const $ = go.GraphObject.make;
  return $(go.Link, {
    routing: go.Routing.Orthogonal,
    corner: 6,
    selectable: false,
    layerName: 'Background',
  });
}
</script>

<style scoped></style>
