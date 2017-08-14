package com.deemons.dor.download.entity;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/11 16:30
 * 包名       com.deemons.network.download.entity
 * 描述
 */

public class DownloadBean {

    public String url;
    public String saveName;
    public String savePath;
    public String extra1;
    public String extra2;
    public String extra3;
    public String extra4;
    public String extra5;

    private DownloadBean() {
    }



    public static class Builder {
        private String url;
        private String saveName;
        private String savePath;
        private String extra1;
        private String extra2;
        private String extra3;
        private String extra4;
        private String extra5;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setSaveName(String saveName) {
            this.saveName = saveName;
            return this;
        }

        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder setExtra1(String extra1) {
            this.extra1 = extra1;
            return this;
        }

        public Builder setExtra2(String extra2) {
            this.extra2 = extra2;
            return this;
        }

        public Builder setExtra3(String extra3) {
            this.extra3 = extra3;
            return this;
        }

        public Builder setExtra4(String extra4) {
            this.extra4 = extra4;
            return this;
        }

        public Builder setExtra5(String extra5) {
            this.extra5 = extra5;
            return this;
        }

        public DownloadBean build() {
            DownloadBean bean = new DownloadBean();
            bean.url = this.url;
            bean.saveName = this.saveName;
            bean.savePath = this.savePath;
            bean.extra1 = this.extra1;
            bean.extra2 = this.extra2;
            bean.extra3 = this.extra3;
            bean.extra4 = this.extra4;
            bean.extra5 = this.extra5;
            return bean;
        }
    }

}
