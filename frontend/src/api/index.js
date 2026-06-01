import goodfriend from './modules/goodfriend'
import group from './modules/group'
import singlemessage from './modules/singlemessage'
import sys from './modules/sys'
import validate from './modules/validate'
import user from './modules/user'
import exportModule from './modules/export'


export default {
  ...user,
  ...goodfriend,
  ...group,
  ...singlemessage,
  ...sys,
  ...validate,
  ...exportModule,
}
