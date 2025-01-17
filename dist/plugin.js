var capacitorScreenRecorder = (function (exports, core) {
    'use strict';

    const ScreenRecorder = core.registerPlugin('ScreenRecorder', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.ScreenRecorderWeb()),
    });

    class ScreenRecorderWeb extends core.WebPlugin {
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

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        ScreenRecorderWeb: ScreenRecorderWeb
    });

    exports.ScreenRecorder = ScreenRecorder;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
