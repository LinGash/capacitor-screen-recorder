import { WebPlugin, PluginListenerHandle } from '@capacitor/core';
import type { ScreenRecorderPlugin } from './definitions';
export declare class ScreenRecorderWeb extends WebPlugin implements ScreenRecorderPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    start(options: any): Promise<any>;
    stop(options: any): Promise<any>;
    recorder_status(options: any): Promise<any>;
    addListener(eventName: string, listenerFunc: (event: any) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
}
