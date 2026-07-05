import { ImageAsset } from '@assets/image.assets';

export interface ModuleInfo {
    name: string;
    icon: ImageAsset;
    tagline: string;
    description: string;
    features: string[];
    techStack: string[];
    accentColor?: string;
}
