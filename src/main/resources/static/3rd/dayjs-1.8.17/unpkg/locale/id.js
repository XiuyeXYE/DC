(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory(require('dayjs')) :
  typeof define === 'function' && define.amd ? define(['dayjs'], factory) :
  (global.dayjs_locale_id = factory(global.dayjs));
}(this, (function (dayjs) { 'use strict';

  dayjs = dayjs && dayjs.hasOwnProperty('default') ? dayjs['default'] : dayjs;

  var locale = {
    name: 'id',
    weekdays: 'Minggu_Senin_Selasa_Rabu_Kamis_Jumat_Sabtu'.split('_'),
    months: 'Januari_Februari_Maret_April_Mei_Juni_Juli_Agustus_September_Oktober_November_Desember'.split('_'),
    weekdaysShort: 'Min_Sen_Sel_Rab_Kam_Jum_Sab'.split('_'),
    monthsShort: 'Jan_Feb_Mar_Apr_Mei_Jun_Jul_Agt_Sep_Okt_Nov_Des'.split('_'),
    weekdaysMin: 'Mg_Sn_Sl_Rb_Km_Jm_Sb'.split('_'),
    weekStart: 1,
    formats: {
      LT: 'HH.mm',
      LTS: 'HH.mm.ss',
      L: 'DD/MM/YYYY',
      LL: 'D MMMM YYYY',
      LLL: 'D MMMM YYYY [pukul] HH.mm',
      LLLL: 'dddd, D MMMM YYYY [pukul] HH.mm'
    },
    relativeTime: {
      future: 'dalam %s',
      past: '%s yang lalu',
      s: 'beberapa detik',
      m: 'semenit',
      mm: '%d menit',
      h: 'sejam',
      hh: '%d jam',
      d: 'sehari',
      dd: '%d hari',
      M: 'sebulan',
      MM: '%d bulan',
      y: 'setahun',
      yy: '%d tahun'
    },
    ordinal: function ordinal(n) {
      return n + ".";
    }
  };
  dayjs.locale(locale, null, true);

  return locale;

})));
