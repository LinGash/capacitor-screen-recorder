import { registerPlugin } from '@capacitor/core';
const ScreenRecorder = registerPlugin('ScreenRecorder', {
    web: () => import('./web').then(m => new m.ScreenRecorderWeb()),
});
export * from './definitions';
export { ScreenRecorder };
//# sourceMappingURL=index.js.map