// 养殖过程相关API - 养殖户端
import { getSeedList, createSeed, updateSeed, deleteSeed } from './seed';
import { getFeedList, createFeed, updateFeed, deleteFeed } from './feed';
import { getMedicineList, createMedicine, updateMedicine, deleteMedicine } from './medicine';
import { getHarvestList, createHarvest, updateHarvest, deleteHarvest } from './harvest';

// 养殖户端API
export {
  getSeedList,
  createSeed,
  updateSeed,
  deleteSeed,
  getFeedList,
  createFeed,
  updateFeed,
  deleteFeed,
  getMedicineList,
  createMedicine,
  updateMedicine,
  deleteMedicine,
  getHarvestList,
  createHarvest,
  updateHarvest,
  deleteHarvest,
};

// 监管局端API
export {
  getManagerSeedList,
  managerAuditSeed,
  managerBatchAuditSeed,
} from './seed';

export {
  getManagerFeedList,
  managerAuditFeed,
  managerBatchAuditFeed,
} from './feed';

export {
  getManagerMedicineList,
  managerAuditMedicine,
  managerBatchAuditMedicine,
} from './medicine';

export {
  getManagerHarvestList,
  managerAuditHarvest,
  managerBatchAuditHarvest,
} from './harvest';
