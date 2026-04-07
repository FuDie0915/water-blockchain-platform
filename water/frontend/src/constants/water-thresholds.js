export const WATER_DATA_TYPES = [
  {
    dataType: 0,
    metricCode: 'TDS',
    label: '总溶解固体',
    unit: 'mg/L',
    normalText: '30000~38000',
    warningText: '25000~30000 / 38000~42000',
    dangerText: '<25000 或 >42000',
  },
  {
    dataType: 1,
    metricCode: 'TURBIDITY',
    label: '浑浊度',
    unit: 'NTU',
    normalText: '≤20',
    warningText: '20~50',
    dangerText: '>50',
  },
  {
    dataType: 2,
    metricCode: 'WATER_TEMP',
    label: '水温',
    unit: '℃',
    normalText: '22~30',
    warningText: '18~22 / 30~32',
    dangerText: '<18 或 >32',
  },
  {
    dataType: 3,
    metricCode: 'SALINITY',
    label: '盐度',
    unit: '‰',
    normalText: '18~32',
    warningText: '15~18 / 32~35',
    dangerText: '<15 或 >35',
  },
  {
    dataType: 4,
    metricCode: 'PH',
    label: 'pH',
    unit: '',
    normalText: '7.8~8.6',
    warningText: '7.5~7.8 / 8.6~9.0',
    dangerText: '<7.5 或 >9.0',
  },
  {
    dataType: 5,
    metricCode: 'DO',
    label: '溶解氧',
    unit: 'mg/L',
    normalText: '≥5',
    warningText: '3~5',
    dangerText: '<3',
  },
  {
    dataType: 6,
    metricCode: 'AMMONIA_N',
    label: '氨氮',
    unit: 'mg/L',
    normalText: '≤0.2',
    warningText: '0.2~0.5',
    dangerText: '>0.5',
  },
  {
    dataType: 7,
    metricCode: 'NITRITE',
    label: '亚硝酸盐',
    unit: 'mg/L',
    normalText: '≤0.1',
    warningText: '0.1~0.2',
    dangerText: '>0.2',
  },
];

export const WATER_WARNING_TYPES = [
  { value: 'water_quality', label: '水质超标' },
  { value: 'environment', label: '环境异常' },
  { value: 'device_offline', label: '设备离线' },
  { value: 'farming_noncompliance', label: '养殖规范异常' },
];

export const METRIC_LEVEL_THEME = {
  normal: 'success',
  warning: 'warning',
  danger: 'danger',
};

function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

export function getWaterMetricLevel(metricCode, value) {
  const num = toNumber(value);
  if (num === null) return 'warning';

  switch (metricCode) {
    case 'WATER_TEMP':
      if (num < 18 || num > 32) return 'danger';
      if (num < 22 || num > 30) return 'warning';
      return 'normal';
    case 'SALINITY':
      if (num < 15 || num > 35) return 'danger';
      if (num < 18 || num > 32) return 'warning';
      return 'normal';
    case 'PH':
      if (num < 7.5 || num > 9.0) return 'danger';
      if (num < 7.8 || num > 8.6) return 'warning';
      return 'normal';
    case 'DO':
      if (num < 3) return 'danger';
      if (num < 5) return 'warning';
      return 'normal';
    case 'AMMONIA_N':
      if (num > 0.5) return 'danger';
      if (num > 0.2) return 'warning';
      return 'normal';
    case 'NITRITE':
      if (num > 0.2) return 'danger';
      if (num > 0.1) return 'warning';
      return 'normal';
    case 'TURBIDITY':
      if (num > 50) return 'danger';
      if (num > 20) return 'warning';
      return 'normal';
    case 'TDS':
      if (num < 25000 || num > 42000) return 'danger';
      if (num < 30000 || num > 38000) return 'warning';
      return 'normal';
    default:
      return 'normal';
  }
}
