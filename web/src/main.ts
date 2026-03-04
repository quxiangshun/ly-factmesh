import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus, {
  ElButton,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElDatePicker,
  ElPagination,
  ElFormItem,
  ElCheckbox,
  ElCheckboxGroup,
  ElRadio,
  ElRadioGroup,
  ElTag,
  ElTransfer,
  ElCascader,
  ElAutocomplete,
} from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';

import './styles/index.css';
import './styles/element-overrides.css';

// 全局使用 small 尺寸
ElButton.setPropsDefaults({ size: 'small' });
ElInput.setPropsDefaults({ size: 'small' });
ElInputNumber.setPropsDefaults({ size: 'small' });
ElSelect.setPropsDefaults({ size: 'small' });
ElDatePicker.setPropsDefaults({ size: 'small' });
ElPagination.setPropsDefaults({ size: 'small' });
ElFormItem.setPropsDefaults({ size: 'small' });
ElCheckbox.setPropsDefaults({ size: 'small' });
ElCheckboxGroup.setPropsDefaults({ size: 'small' });
ElRadio.setPropsDefaults({ size: 'small' });
ElRadioGroup.setPropsDefaults({ size: 'small' });
ElTag.setPropsDefaults({ size: 'small' });
ElTransfer.setPropsDefaults({ size: 'small' });
ElCascader.setPropsDefaults({ size: 'small' });
ElAutocomplete.setPropsDefaults({ size: 'small' });

const app = createApp(App);

app.use(router);
app.use(ElementPlus, { locale: zhCn });

app.mount('#app');

