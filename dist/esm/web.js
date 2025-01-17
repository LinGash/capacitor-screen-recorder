import { WebPlugin } from '@capacitor/core';
export class ScreenRecorderWeb extends WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return { value: options.value };
    }
    async start(options) {
        console.log('startFunctionCalled', options);
        throw this.unimplemented('The start method is not implemented on the web platform.');
    }
    async stop(options) {
        console.log('stopFunctionCalled', options);
        throw this.unimplemented('The stop method is not implemented on the web platform.');
    }
    async recorder_status(options) {
        console.log('recorderStatusFunctionCalled', options);
        throw this.unimplemented('The recorder_status method is not implemented on the web platform.');
    }
    addListener(eventName, listenerFunc) {
        const handle = super.addListener(eventName, listenerFunc);
        return Object.assign(handle, {
            remove: async () => {
                console.log(`Listener for ${eventName} removed`);
                (await handle).remove();
            },
        });
    }
}
//# sourceMappingURL=web.js.map