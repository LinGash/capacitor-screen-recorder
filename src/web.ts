import { WebPlugin, PluginListenerHandle } from '@capacitor/core';
import type { ScreenRecorderPlugin } from './definitions';

export class ScreenRecorderWeb
  extends WebPlugin
  implements ScreenRecorderPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return { value: options.value };
  }

  async start(options: any): Promise<any> {
    console.log('startFunctionCalled', options);
    throw this.unimplemented('The start method is not implemented on the web platform.');
  }

  async stop(options: any): Promise<any> {
    console.log('stopFunctionCalled', options);
    throw this.unimplemented('The stop method is not implemented on the web platform.');
  }

  async recorder_status(options: any): Promise<any> {
    console.log('recorderStatusFunctionCalled', options);
    throw this.unimplemented('The recorder_status method is not implemented on the web platform.');
  }

  addListener(
    eventName: string,
    listenerFunc: (event: any) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle {
    const handle = super.addListener(eventName, listenerFunc);
    return Object.assign(handle, {
      remove: async () => {
        console.log(`Listener for ${eventName} removed`);
        (await handle).remove();
      },
    });
  }
}
