import { create } from 'zustand';
import { storage } from '../utils/storage';
import client from '../api/client';

interface User {
  userId: string;
  name: string;
  email: string;
  region: string;
}

interface StoreState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  products: any[];
  cartItems: any[];
  cartOptimization: any | null;
  alerts: any[];
  unreadCount: number;
  unreadAlertCount: number;
  userRegion: string;
  userStores: string[];

  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  setProducts: (products: any[]) => void;
  setCartItems: (items: any[]) => void;
  setCartOptimization: (opt: any) => void;
  setAlerts: (alerts: any[]) => void;
  setUnreadCount: (count: number) => void;
  setUnreadAlertCount: (count: number) => void;
  setUserRegion: (region: string) => void;
  setUserStores: (stores: string[]) => void;
  addUserStore: (storeId: string) => void;
  removeUserStore: (storeId: string) => void;
  loadPersistedAuth: () => Promise<void>;
}

const useStore = create<StoreState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  products: [],
  cartItems: [],
  cartOptimization: null,
  alerts: [],
  unreadCount: 0,
  unreadAlertCount: 0,
  userRegion: 'national',
  userStores: ['walmart', 'kroger'],

  loadPersistedAuth: async () => {
    const token = await storage.getItem('jwt_token');
    const userStr = await storage.getItem('user');
    if (token && userStr) {
      const user = JSON.parse(userStr);
      set({
        token,
        user,
        isAuthenticated: true,
      });

      try {
        const [storesRes, countRes] = await Promise.all([
          client.get('/user/stores'),
          client.get('/alerts/count'),
        ]);
        set({
          userStores: storesRes.data.storeIds || ['walmart', 'kroger'],
          unreadAlertCount: countRes.data.unreadCount || 0,
        });
      } catch {
        // Keep defaults if API fails
      }
    }
  },

  login: async (email, password) => {
    const res = await client.post('/auth/login', { email, password });
    const { token, userId, name } = res.data;
    const user = { userId, name, email, region: 'national' };
    await storage.setItem('jwt_token', token);
    await storage.setItem('user', JSON.stringify(user));
    set({ token, user, isAuthenticated: true });
  },

  logout: async () => {
    await storage.removeItem('jwt_token');
    await storage.removeItem('user');
    set({
      token: null,
      user: null,
      isAuthenticated: false,
      unreadAlertCount: 0,
      alerts: [],
    });
  },

  setProducts: (products) => set({ products }),
  setCartItems: (cartItems) => set({ cartItems }),
  setCartOptimization: (cartOptimization) => set({ cartOptimization }),
  setAlerts: (alerts) => set({ alerts }),
  setUnreadCount: (unreadCount) => set({ unreadCount }),
  setUnreadAlertCount: (unreadAlertCount) => set({ unreadAlertCount }),
  setUserRegion: (userRegion) => set({ userRegion }),

  setUserStores: (userStores) => set({ userStores }),

  addUserStore: (storeId) =>
    set((state) => ({
      userStores: state.userStores.includes(storeId)
        ? state.userStores
        : [...state.userStores, storeId],
    })),

  removeUserStore: (storeId) =>
    set((state) => ({
      userStores: state.userStores.filter((id) => id !== storeId),
    })),
}));

export default useStore;
