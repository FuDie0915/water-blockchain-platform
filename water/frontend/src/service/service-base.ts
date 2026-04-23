import { TdBaseTableProps } from 'tdesign-vue';

interface DashboardPanel {
  title: string;
  number: string | number;
  leftType: string;
  upTrend?: string;
  downTrend?: string;
}

interface TendItem {
  growUp?: number;
  productName: string;
  count: number;
  date: string;
}

export const PANE_LIST: Array<DashboardPanel> = [
  {
    title: '区块高度',
    number: '0',
    upTrend: '20.5%',
    leftType: 'echarts-line',
  },
  {
    title: '节点数量',
    number: '0',
    downTrend: '20.5%',
    leftType: 'echarts-bar',
  },
  {
    title: '交易数量',
    number: '0',
    downTrend: '20.5%',
    leftType: 'icon-usergroup',
  },
  {
    title: '待交易数量',
    number: '0',
    downTrend: '20.5%',
    leftType: 'icon-file-paste',
  },
];

export const SALE_TEND_LIST: Array<TendItem> = [];

export const BUY_TEND_LIST: Array<TendItem> = [];

export const SALE_COLUMNS: TdBaseTableProps['columns'] = [
  {
    align: 'center',
    colKey: 'index',
    title: '排名',
    width: 80,
    fixed: 'left',
  },
  {
    align: 'left',
    ellipsis: true,
    colKey: 'productName',
    title: '客户名称',
    minWidth: 200,
  },
  {
    align: 'center',
    colKey: 'growUp',
    width: 100,
    title: '较上周',
  },
  {
    align: 'center',
    colKey: 'count',
    title: '订单量',
    width: 100,
  },
  {
    align: 'center',
    colKey: 'date',
    width: 140,
    title: '合同签订日期',
  },
  {
    align: 'center',
    colKey: 'operation',
    title: '操作',
    width: 80,
    fixed: 'right',
  },
];

export const BUY_COLUMNS: TdBaseTableProps['columns'] = [
  {
    align: 'center',
    colKey: 'index',
    title: '排名',
    width: 80,
    fixed: 'left',
  },
  {
    align: 'left',
    ellipsis: true,
    colKey: 'productName',
    title: '供应商名称',
    minWidth: 200,
  },
  {
    align: 'center',
    colKey: 'growUp',
    width: 100,
    title: '较上周',
  },
  {
    align: 'center',
    colKey: 'count',
    title: '订单量',
    width: 100,
  },
  {
    align: 'center',
    colKey: 'date',
    width: 140,
    title: '合同签订日期',
  },
  {
    align: 'center',
    colKey: 'operation',
    title: '操作',
    width: 80,
    fixed: 'right',
  },
];
