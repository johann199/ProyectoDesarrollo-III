import { environment } from "../../../environments/environment";

export const apiConstants = {
        BASE_URL: environment.apiUrl,
        LOGIN: '/auth/login',
        ATTENDANCES: '/attendances',
        LIST_ATTENDANCES: '/attendances',
        LABORATORIES: '/laboratories',
        ASSIGN_USER_ROLE: '/users/:userCode/role',
        LIST_USER: '/users',
        PRACTICES: '/practices',
        LOANS: '/loans',
};