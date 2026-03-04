const { request, uploadFile, checkLogin } = require('../../utils/request');
const QQMapWX = require('../../utils/qqmap-wx-jssdk.min.js');
const app = getApp();

const TEMP_MARKER_ID = 99999;
const YEARS = [];
for (let y = 2015; y <= 2026; y++) YEARS.push(y);
const MONTHS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
const ITEM_WIDTH = 120; // rpx

function buildDays(year, month) {
  const count = new Date(year, month, 0).getDate();
  const arr = [];
  for (let d = 1; d <= count; d++) arr.push(d);
  return arr;
}

Page({
  data: {
    latitude: 30.5554,
    longitude: 114.3162,
    markers: [],
    photoMarkers: [],
    scale: 14,
    // 时光穿梭
    filterLabel: '全部时间',
    filterActive: false,
    showFilterPanel: false,
    panelTab: 'day',
    rangeStart: '',
    rangeEnd: '',
    years: YEARS,
    months: MONTHS,
    selectedYear: new Date().getFullYear(),
    selectedMonth: new Date().getMonth() + 1,
    selectedDay: new Date().getDate(),
    days: buildDays(new Date().getFullYear(), new Date().getMonth() + 1),
    yearScrollLeft: 0,
    monthScrollLeft: 0,
    dayScrollLeft: 0,
    // 定位状态
    hasLocation: false,
    districtName: '',
    // 底部操作栏
    showActionBar: false,
    tapLat: 0,
    tapLng: 0,
    tapLocationName: '',
    uploadDate: '',
    uploadDateDisplay: '今天',
    // 上传状态
    uploading: false,
    uploadToast: '',
    showToast: false
  },

  onLoad() {
    this.mapCtx = wx.createMapContext('map');
    this._loading = false;
    this._loadTimer = null;
    this._historyMarkers = [];
    this._photoIdMap = {};
    this._qqmapsdk = new QQMapWX({ key: app.globalData.mapKey });
    this._yearScrollTimer = null;
    this._monthScrollTimer = null;
    this._dayScrollTimer = null;

    const sysInfo = wx.getWindowInfo();
    this._rpxRatio = sysInfo.windowWidth / 750;
    this._itemPx = ITEM_WIDTH * this._rpxRatio;
    this._halfScreen = sysInfo.windowWidth / 2;

    const today = this._formatDate(new Date());
    this.setData({ uploadDate: today, uploadDateDisplay: '今天' });
    this.getCurrentLocation();
  },

  onShow() {
    this.debouncedLoadPhotos();
  },

  // ========== 滚轮居中计算 ==========

  /** 计算让第 idx 个 item 居中的 scrollLeft（px） */
  _centerScrollLeft(idx) {
    // pad = halfScreen - itemPx/2
    // item[idx] 左边缘 = pad + idx * itemPx
    // item[idx] 中心 = pad + idx * itemPx + itemPx/2 = halfScreen + idx * itemPx
    // 要让中心对齐视口中心(halfScreen)：scrollLeft = halfScreen + idx*itemPx - halfScreen = idx * itemPx
    return Math.max(0, idx * this._itemPx);
  },

  _scrollYearTo(year) {
    const idx = YEARS.indexOf(year);
    if (idx < 0) return;
    const sl = this._centerScrollLeft(idx);
    // 微信小程序 scroll-left 相同值不会重新触发动画，加微小偏移
    this.setData({ yearScrollLeft: sl + 0.1 });
    setTimeout(() => { this.setData({ yearScrollLeft: sl }); }, 20);
  },

  _scrollMonthTo(month) {
    const idx = month - 1;
    const sl = this._centerScrollLeft(idx);
    this.setData({ monthScrollLeft: sl + 0.1 });
    setTimeout(() => { this.setData({ monthScrollLeft: sl }); }, 20);
  },

  _scrollDayTo(day) {
    const idx = day - 1;
    const sl = this._centerScrollLeft(idx);
    this.setData({ dayScrollLeft: sl + 0.1 });
    setTimeout(() => { this.setData({ dayScrollLeft: sl }); }, 20);
  },

  /** 根据年月重新生成日期数组，并重置选中日 */
  _updateDays(year, month, resetDay) {
    const days = buildDays(year, month);
    const day = resetDay ? 1 : Math.min(this.data.selectedDay, days.length);
    this.setData({ days, selectedDay: day });
    this._scrollDayTo(day);
  },

  // ========== 定位 ==========

  getCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude,
          hasLocation: true
        });
        this._currentLat = res.latitude;
        this._currentLng = res.longitude;
        this._userLat = res.latitude;
        this._userLng = res.longitude;
        this._updateDistrict(res.latitude, res.longitude);
        this.debouncedLoadPhotos();
      },
      fail: () => {
        this._showToast('未获取定位，可手动点击地图选点');
      }
    });
  },

  onLocateTap() {
    if (!this.data.hasLocation) {
      this.getCurrentLocation();
      return;
    }
    this.mapCtx.moveToLocation({
      latitude: this._userLat,
      longitude: this._userLng
    });
  },

  // ========== 时光穿梭 ==========

  onFilterTap() {
    if (!this.data.showFilterPanel) {
      // 打开面板时，如果是当天 tab 且没有快捷选项，滚动到选中项
      if (this.data.panelTab === 'day' && !this.data.activeQuick) {
        setTimeout(() => {
          this._scrollYearTo(this.data.selectedYear);
          this._scrollMonthTo(this.data.selectedMonth);
          this._scrollDayTo(this.data.selectedDay);
        }, 350);
      }
    }
    this.setData({ showFilterPanel: !this.data.showFilterPanel });
  },

  onCloseFilter() {
    this.setData({ showFilterPanel: false });
  },

  onMaskTap() {
    this.setData({ showFilterPanel: false });
  },

  /** Tab 切换 */
  onTabAll() {
    this.setData({ panelTab: 'all' });
  },

  onTabDay() {
    this.setData({ panelTab: 'day' });
    setTimeout(() => {
      this._scrollYearTo(this.data.selectedYear);
      this._scrollMonthTo(this.data.selectedMonth);
      this._scrollDayTo(this.data.selectedDay);
    }, 50);
  },

  onTabRange() {
    this.setData({ panelTab: 'range' });
  },

  /** 区间日期选择 */
  onRangeStartChange(e) {
    this.setData({ rangeStart: e.detail.value });
  },

  onRangeEndChange(e) {
    this.setData({ rangeEnd: e.detail.value });
  },

  /** 年份滚动 — 防抖吸附 */
  onYearScroll(e) {
    if (this._yearScrollTimer) clearTimeout(this._yearScrollTimer);
    this._yearScrollTimer = setTimeout(() => {
      this._snapYear(e.detail.scrollLeft);
    }, 200);
  },

  _snapYear(scrollLeft) {
    // item[idx] 居中时 scrollLeft = idx * itemPx
    let idx = Math.round(scrollLeft / this._itemPx);
    idx = Math.max(0, Math.min(idx, YEARS.length - 1));

    if (YEARS[idx] !== this.data.selectedYear) {
      this.setData({ selectedYear: YEARS[idx] });
      this._scrollYearTo(YEARS[idx]);
      this._updateDays(YEARS[idx], this.data.selectedMonth, false);
    } else {
      // 吸附回正确位置
      this._scrollYearTo(YEARS[idx]);
    }
  },

  /** 月份滚动 — 防抖吸附 */
  onMonthScroll(e) {
    if (this._monthScrollTimer) clearTimeout(this._monthScrollTimer);
    this._monthScrollTimer = setTimeout(() => {
      this._snapMonth(e.detail.scrollLeft);
    }, 200);
  },

  _snapMonth(scrollLeft) {
    let idx = Math.round(scrollLeft / this._itemPx);
    idx = Math.max(0, Math.min(idx, MONTHS.length - 1));

    if (MONTHS[idx] !== this.data.selectedMonth) {
      this.setData({
        selectedMonth: MONTHS[idx]
      });
      this._scrollMonthTo(MONTHS[idx]);
      this._updateDays(this.data.selectedYear, MONTHS[idx], false);
    } else {
      this._scrollMonthTo(MONTHS[idx]);
    }
  },

  /** 点击年份 item */
  onYearItemTap(e) {
    const year = e.currentTarget.dataset.year;
    this.setData({ selectedYear: year });
    this._scrollYearTo(year);
    this._updateDays(year, this.data.selectedMonth, false);
  },

  /** 点击月份 item */
  onMonthItemTap(e) {
    const month = e.currentTarget.dataset.month;
    this.setData({
      selectedMonth: month
    });
    this._scrollMonthTo(month);
    this._updateDays(this.data.selectedYear, month, false);
  },

  /** 日期滚动 — 防抖吸附 */
  onDayScroll(e) {
    if (this._dayScrollTimer) clearTimeout(this._dayScrollTimer);
    this._dayScrollTimer = setTimeout(() => {
      this._snapDay(e.detail.scrollLeft);
    }, 200);
  },

  _snapDay(scrollLeft) {
    const days = this.data.days;
    let idx = Math.round(scrollLeft / this._itemPx);
    idx = Math.max(0, Math.min(idx, days.length - 1));

    if (days[idx] !== this.data.selectedDay) {
      this.setData({ selectedDay: days[idx] });
      this._scrollDayTo(days[idx]);
    } else {
      this._scrollDayTo(days[idx]);
    }
  },

  /** 点击日期 item */
  onDayItemTap(e) {
    const day = e.currentTarget.dataset.day;
    this.setData({ selectedDay: day });
    this._scrollDayTo(day);
  },

  /** 确认穿梭 */
  onFilterConfirm() {
    const { panelTab, selectedYear, selectedMonth, selectedDay, rangeStart, rangeEnd } = this.data;
    let startDate = '';
    let endDate = '';
    let label = '全部时间';
    let filterActive = false;

    if (panelTab === 'all') {
      // 全部时间，不传日期
    } else if (panelTab === 'range') {
      if (rangeStart) startDate = rangeStart;
      if (rangeEnd) endDate = rangeEnd;
      if (startDate && endDate) {
        label = startDate + ' ~ ' + endDate;
        filterActive = true;
      } else if (startDate) {
        label = startDate + ' 起';
        filterActive = true;
      } else if (endDate) {
        label = '至 ' + endDate;
        filterActive = true;
      }
    } else {
      // 回忆当天
      const m = String(selectedMonth).padStart(2, '0');
      const dd = String(selectedDay).padStart(2, '0');
      const dateStr = selectedYear + '-' + m + '-' + dd;
      startDate = dateStr;
      endDate = dateStr;
      label = selectedYear + '年' + selectedMonth + '月' + selectedDay + '日';
      filterActive = true;
    }

    this._filterStartDate = startDate;
    this._filterEndDate = endDate;
    this.setData({
      filterLabel: label,
      filterActive: filterActive,
      showFilterPanel: false
    });
    this.loadNearbyPhotos();
  },

  /** 重置 */
  onFilterReset() {
    const now = new Date();
    this.setData({
      panelTab: 'day',
      rangeStart: '',
      rangeEnd: '',
      selectedYear: now.getFullYear(),
      selectedMonth: now.getMonth() + 1,
      selectedDay: now.getDate(),
      days: buildDays(now.getFullYear(), now.getMonth() + 1)
    });
    setTimeout(() => {
      this._scrollYearTo(now.getFullYear());
      this._scrollMonthTo(now.getMonth() + 1);
      this._scrollDayTo(now.getDate());
    }, 50);
  },

  // ========== 加载照片标记 ==========

  debouncedLoadPhotos() {
    if (this._loadTimer) clearTimeout(this._loadTimer);
    this._loadTimer = setTimeout(() => { this.loadNearbyPhotos(); }, 500);
  },

  loadNearbyPhotos() {
    if (this._loading) return;
    this._loading = true;
    const latitude = this._currentLat || this.data.latitude;
    const longitude = this._currentLng || this.data.longitude;
    const params = { latitude, longitude, radius: 10 };

    if (this._filterStartDate) params.startDate = this._filterStartDate;
    if (this._filterEndDate) params.endDate = this._filterEndDate;

    request('/photo/nearby', 'GET', params)
      .then((res) => {
        const photos = res.data || [];
        this._photoIdMap = {};

        const groups = {};
        photos.forEach((item) => {
          const key = item.latitude.toFixed(4) + ',' + item.longitude.toFixed(4);
          if (!groups[key]) groups[key] = [];
          groups[key].push(item);
        });

        let markerId = 1;
        this._historyMarkers = [];
        const photoMarkers = [];
        Object.keys(groups).forEach((key) => {
          const group = groups[key];
          const first = group[0];
          const id = markerId++;
          this._photoIdMap[id] = first.id;

          const thumbUrl = first.thumbnailUrl || first.imageUrl || '';
          const count = group.length;

          photoMarkers.push({
            id,
            thumbUrl,
            count,
            locationName: first.locationName || ''
          });

          this._historyMarkers.push({
            id, latitude: first.latitude, longitude: first.longitude,
            iconPath: '/images/marker-transparent.png', width: 1, height: 1,
            customCallout: {
              anchorY: 0,
              anchorX: 0,
              display: 'ALWAYS'
            }
          });
        });
        this.setData({ photoMarkers });
        this._rebuildMarkers();
      })
      .catch((err) => { console.log('加载附近照片失败', err); })
      .finally(() => { this._loading = false; });
  },

  // ========== 地图选点 ==========

  onMapTap(e) {
    if (this.data.uploading) return;
    if (this.data.showFilterPanel) {
      this.setData({ showFilterPanel: false });
      return;
    }
    let lat, lng;
    if (e.detail && e.detail.latitude !== undefined) {
      lat = e.detail.latitude;
      lng = e.detail.longitude;
    } else {
      return;
    }
    this.setData({
      tapLat: lat, tapLng: lng,
      showActionBar: true, tapLocationName: '获取地址中...'
    });
    this._rebuildMarkers();
    this._reverseGeocode(lat, lng);
    this._showToast('已选上传位置');
  },

  _reverseGeocode(lat, lng) {
    this._qqmapsdk.reverseGeocoder({
      location: { latitude: lat, longitude: lng },
      success: (res) => {
        const addr = res.result;
        const name = addr.formatted_addresses
          ? addr.formatted_addresses.recommend
          : addr.address;
        this.setData({ tapLocationName: name || addr.address });
      },
      fail: (err) => {
        console.error('[Map] 逆地理编码失败', err);
        this.setData({
          tapLocationName: lat.toFixed(4) + ', ' + lng.toFixed(4)
        });
      }
    });
  },

  _rebuildMarkers() {
    let markers = [...(this._historyMarkers || [])];
    if (this.data.showActionBar && this.data.tapLat) {
      markers.push({
        id: TEMP_MARKER_ID,
        latitude: this.data.tapLat,
        longitude: this.data.tapLng,
        iconPath: '/images/marker-temp.png',
        width: 44, height: 44,
        callout: {
          content: '上传位置', color: '#07c160', fontSize: 13,
          borderRadius: 8, padding: 6, display: 'ALWAYS',
          bgColor: '#fff', borderWidth: 1, borderColor: '#07c160'
        }
      });
    }
    this.setData({ markers });
  },

  // ========== 底部操作栏 ==========

  onUploadDateChange(e) {
    const date = e.detail.value;
    const today = this._formatDate(new Date());
    this.setData({
      uploadDate: date,
      uploadDateDisplay: (date === today) ? '今天' : date
    });
  },

  onCancelTap() {
    this.setData({ showActionBar: false, tapLat: 0, tapLng: 0, tapLocationName: '' });
    this._rebuildMarkers();
  },

  onGoUpload() {
    if (!checkLogin()) return;
    const { tapLat, tapLng, uploadDate, tapLocationName } = this.data;
    if (!uploadDate) { this._showToast('请选择日期'); return; }

    wx.chooseMedia({
      count: 1, mediaType: ['image'], sizeType: ['compressed'],
      success: (res) => {
        this._doUpload(res.tempFiles[0].tempFilePath, tapLat, tapLng, uploadDate, tapLocationName);
      }
    });
  },

  _doUpload(filePath, lat, lng, photoDate, locationName) {
    this.setData({ uploading: true, showActionBar: false });
    this._showToast('正在上传…');

    uploadFile('/photo/upload', filePath, {
      longitude: String(lng), latitude: String(lat),
      photoDate: photoDate, locationName: locationName || '', description: ''
    })
      .then(() => {
        this._showToast('上传成功，已加入时光地图');
        this.setData({ tapLat: 0, tapLng: 0 });
        this.loadNearbyPhotos();
      })
      .catch(() => {
        this._showToast('上传失败，请重试');
        this.setData({ showActionBar: true });
        this._rebuildMarkers();
      })
      .finally(() => { this.setData({ uploading: false }); });
  },

  // ========== 标记与地图事件 ==========

  onMarkerTap(e) {
    const markerId = e.markerId;
    if (markerId === TEMP_MARKER_ID) return;
    const photoId = this._photoIdMap && this._photoIdMap[markerId];
    if (photoId) {
      wx.navigateTo({ url: '/pages/detail/detail?id=' + photoId });
    }
  },

  onRegionChange(e) {
    if (e.type === 'end') {
      this.mapCtx.getCenterLocation({
        success: (res) => {
          this._currentLat = res.latitude;
          this._currentLng = res.longitude;
          this._updateDistrict(res.latitude, res.longitude);
          this.debouncedLoadPhotos();
        }
      });
    }
  },

  // ========== 底部悬浮栏 ==========

  onChatTap() {
    this._showToast('聊天功能即将上线');
  },

  onCommunityTap() {
    this._showToast('社区功能即将上线');
  },

  onProfileTap() {
    wx.navigateTo({ url: '/pages/profile/profile' });
  },

  /** 根据经纬度更新顶部区县名 */
  _updateDistrict(lat, lng) {
    this._qqmapsdk.reverseGeocoder({
      location: { latitude: lat, longitude: lng },
      success: (res) => {
        const comp = res.result.address_component || {};
        const district = comp.district || comp.city || '';
        this.setData({ districtName: district });
      },
      fail: () => {}
    });
  },

  // ========== 工具方法 ==========

  _formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + d;
  },

  _showToast(msg) {
    this.setData({ uploadToast: msg, showToast: true });
    if (this._toastTimer) clearTimeout(this._toastTimer);
    this._toastTimer = setTimeout(() => { this.setData({ showToast: false }); }, 2500);
  }
});
