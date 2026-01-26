export interface ProjectSummaryCard {
  id: string;
  title: string;
  value: string;
  description?: string;
  trend?: {
    label: string;
    direction: 'up' | 'down' | 'flat';
  };
}

export const projectSummaryCards: ProjectSummaryCard[] = [
  {
    id: 'total',
    title: 'Total Projects',
    value: '42',
    description: 'Sample data for UI layout',
    trend: {
      label: '+3 vs last month',
      direction: 'up',
    },
  },
  {
    id: 'active',
    title: 'In Progress',
    value: '18',
    description: 'Active contracts this quarter',
  },
  {
    id: 'completed',
    title: 'Completed',
    value: '21',
    description: 'Projects closed in the last 12 months',
  },
  {
    id: 'contract',
    title: 'Total Contract Value',
    value: 'KRW 12.4B',
    description: 'Sample revenue total',
    trend: {
      label: '+8% QoQ',
      direction: 'up',
    },
  },
];
