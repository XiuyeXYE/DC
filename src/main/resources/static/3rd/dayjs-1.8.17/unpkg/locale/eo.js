(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory(require('dayjs')) :
  typeof define === 'function' && define.amd ? define(['dayjs'], factory) :
  (global.dayjs_locale_eo = factory(global.dayjs));
}(this, (function (dayjs) { 'use strict';

  dayjs = dayjs && dayjs.hasOwnProperty('default') ? dayjs['default'] : dayjs;

  var locale = {
    name: 'eo',
    weekdays: 'dimanĉo_lundo_mardo_merkredo_ĵaŭdo_vendredo_sabato'.split('_'),
    months: 'januaro_februaro_marto_aprilo_majo_junio_julio_aŭgusto_septembro_oktobro_novembro_decembro'.split('_'),
    weekStart: 1,
    weekdaysShort: 'dim_lun_mard_merk_ĵaŭ_ven_sab'.split('_'),
    monthsShort: 'jan_feb_mar_apr_maj_jun_jul_aŭg_sep_okt_nov_dec'.split('_'),
    weekdaysMin: 'di_lu_ma_me_ĵa_ve_sa'.split('_'),
    ordinal: function ordinal(n) {
      return n;
    },
    formats: {
      LT: 'HH:mm',
      LTS: 'HH:mm:ss',
      L: 'YYYY-MM-DD',
      LL: 'D[-a de] MMMM, YYYY',
      LLL: 'D[-a de] MMMM, YYYY HH:mm',
      LLLL: 'dddd, [la] D[-a de] MMMM, YYYY HH:mm'
    },
    relativeTime: {
      future: 'post %s',
      past: 'antaŭ %s',
      s: 'sekundoj',
      m: 'minuto',
      mm: '%d minutoj',
      h: 'horo',
      hh: '%d horoj',
      d: 'tago',
      dd: '%d tagoj',
      M: 'monato',
      MM: '%d monatoj',
      y: 'jaro',
      yy: '%d jaroj'
    }
  };
  dayjs.locale(locale, null, true);

  return locale;

})));
