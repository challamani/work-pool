import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { User, MapPin, Star, Edit2, CheckCircle, Briefcase } from 'lucide-react';

const INDIA_STATES = [
  'Andhra Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi', 'Gujarat',
  'Haryana', 'Karnataka', 'Kerala', 'Maharashtra', 'Rajasthan', 'Tamil Nadu',
  'Telangana', 'Uttar Pradesh', 'West Bengal',
];

const SKILL_SUGGESTIONS = [
  'Plumbing', 'Electrical', 'Carpentry', 'Painting', 'Cleaning', 'Cooking',
  'Teaching', 'Driving', 'Photography', 'Marketing', 'Coding', 'Accounting',
  'Gardening', 'Tailoring', 'Welding', 'AC Repair', 'Computer Repair',
];

const ProfilePage: React.FC = () => {
  const { user, updateUser } = useAuthStore();
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [skillInput, setSkillInput] = useState('');
  const [form, setForm] = useState({
    fullName: user?.fullName ?? '',
    phoneNumber: user?.phoneNumber ?? '',
    bio: user?.bio ?? '',
    serviceRadiusKm: user?.serviceRadiusKm ?? 20,
    skills: user?.skills ? [...user.skills] : [],
    location: user?.location ?? { city: '', district: '', state: '', pincode: '', latitude: 0, longitude: 0 },
  });

  const { data, isLoading } = useQuery({
    queryKey: ['profile', 'me'],
    queryFn: () => userApi.getMe(),
  });

  const mutation = useMutation({
    mutationFn: () => userApi.updateProfile(form),
    onSuccess: (res) => {
      if (res.data.data) {
        updateUser(res.data.data);
      }
      queryClient.invalidateQueries({ queryKey: ['profile', 'me'] });
      setEditing(false);
    },
  });

  const profile = data?.data?.data ?? user;

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />;

  const addSkill = (s: string) => {
    const sk = s.trim();
    if (sk && !form.skills.includes(sk)) setForm({ ...form, skills: [...form.skills, sk] });
    setSkillInput('');
  };

  const initials = profile?.fullName
    ? profile.fullName.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">

      {/* Profile hero card */}
      <div className="card overflow-hidden">
        {/* Gradient banner */}
        <div className="h-24 bg-gradient-to-r from-brand-600 via-indigo-600 to-ocean-600 relative">
          <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_30%_50%,white,transparent)]" />
        </div>

        <div className="px-6 pb-6">
          {/* Avatar (overlaps banner) */}
          <div className="flex items-end justify-between -mt-10 mb-4">
            <div>
              {profile?.profileImageUrl ? (
                <img src={profile.profileImageUrl} alt={profile.fullName}
                  className="w-20 h-20 rounded-2xl object-cover ring-4 ring-white shadow-brand" />
              ) : (
                <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-brand-500 to-indigo-600 flex items-center justify-center ring-4 ring-white shadow-brand">
                  <span className="text-2xl font-extrabold text-white">{initials}</span>
                </div>
              )}
            </div>
            <button onClick={() => setEditing(!editing)} className="btn-secondary text-sm py-1.5 px-4 gap-1.5">
              <Edit2 className="w-3.5 h-3.5" />{editing ? 'Cancel' : 'Edit Profile'}
            </button>
          </div>

          <div className="space-y-2">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl font-extrabold text-slate-900">{profile?.fullName}</h1>
              {profile?.aadhaarVerification === 'VERIFIED' && (
                <span className="flex items-center gap-1 bg-emerald-50 text-emerald-700 text-xs font-bold px-2.5 py-0.5 rounded-full border border-emerald-200">
                  <CheckCircle className="w-3 h-3" /> Verified
                </span>
              )}
            </div>
            <p className="text-sm text-slate-400">{profile?.email}</p>

            {(profile?.averageRating ?? 0) > 0 && (
              <div className="flex items-center gap-1.5">
                {[1,2,3,4,5].map((i) => (
                  <Star key={i}
                    className={`w-4 h-4 ${i <= Math.round(profile?.averageRating ?? 0) ? 'text-amber-400 fill-amber-400' : 'text-slate-200 fill-slate-200'}`} />
                ))}
                <span className="text-sm font-bold text-slate-700 ml-1">{(profile?.averageRating ?? 0).toFixed(1)}</span>
                <span className="text-xs text-slate-400">({profile?.totalRatings ?? 0} ratings)</span>
              </div>
            )}

            {profile?.location && (
              <div className="flex items-center gap-1.5 text-sm text-slate-500">
                <MapPin className="w-4 h-4 text-ocean-500" />
                {profile.location.city}, {profile.location.district}, {profile.location.state}
                {profile.serviceRadiusKm > 0 && (
                  <span className="text-slate-400">· {profile.serviceRadiusKm} km radius</span>
                )}
              </div>
            )}

            {profile?.bio && <p className="text-sm text-slate-600 leading-relaxed">{profile.bio}</p>}

            {profile?.skills && profile.skills.length > 0 && (
              <div className="flex flex-wrap gap-1.5 pt-1">
                {profile.skills.map((s: string) => (
                  <span key={s} className="bg-brand-50 text-brand-700 text-xs px-2.5 py-1 rounded-full border border-brand-200 font-semibold">{s}</span>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Edit form */}
      {editing && (
        <div className="card p-6 space-y-5">
          <h2 className="font-bold text-slate-900">Edit Profile</h2>
          <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1.5">Full Name</label>
                <input className="input" value={form.fullName}
                  onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1.5">Phone</label>
                <input className="input" value={form.phoneNumber}
                  onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
              </div>
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Bio</label>
              <textarea className="input" rows={3} value={form.bio}
                onChange={(e) => setForm({ ...form, bio: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Service Radius (km)</label>
              <input className="input" type="number" min={1} max={200} value={form.serviceRadiusKm}
                onChange={(e) => setForm({ ...form, serviceRadiusKm: Number(e.target.value) })} />
            </div>

            <fieldset className="space-y-2">
              <legend className="text-sm font-semibold text-slate-700 mb-1.5">Location</legend>
              <div className="grid grid-cols-2 gap-2">
                <input className="input" placeholder="City" value={form.location.city}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, city: e.target.value } })} />
                <input className="input" placeholder="District" value={form.location.district}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, district: e.target.value } })} />
                <select className="input" value={form.location.state}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, state: e.target.value } })}>
                  <option value="">Select State</option>
                  {INDIA_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
                <input className="input" placeholder="Pincode" value={form.location.pincode ?? ''}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, pincode: e.target.value } })} />
              </div>
            </fieldset>

            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1.5">Skills</label>
              <div className="flex gap-2 mb-2">
                <input className="input flex-1" placeholder="Add skill…"
                  value={skillInput} onChange={(e) => setSkillInput(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill(skillInput); }}} />
                <button type="button" onClick={() => addSkill(skillInput)} className="btn-secondary text-sm px-4">Add</button>
              </div>
              <div className="flex flex-wrap gap-1.5 mb-2">
                {form.skills.map((s) => (
                  <span key={s} className="bg-brand-100 text-brand-700 text-xs px-2.5 py-1 rounded-full flex items-center gap-1 border border-brand-200 font-semibold">
                    {s}
                    <button type="button" onClick={() => setForm({ ...form, skills: form.skills.filter((x) => x !== s) })}
                      className="text-brand-400 hover:text-red-500 font-bold">×</button>
                  </span>
                ))}
              </div>
              <div className="flex flex-wrap gap-1.5">
                {SKILL_SUGGESTIONS.filter((s) => !form.skills.includes(s)).map((s) => (
                  <button type="button" key={s} onClick={() => addSkill(s)}
                    className="text-xs border border-slate-200 rounded-full px-2.5 py-0.5 text-slate-600 hover:bg-brand-50 hover:border-brand-200 hover:text-brand-700 transition-colors">
                    + {s}
                  </button>
                ))}
              </div>
            </div>

            <button type="submit" disabled={mutation.isPending} className="btn-primary w-full py-3">
              {mutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Save Changes'}
            </button>
          </form>
        </div>
      )}

      {/* Quick-links */}
      <div className="grid grid-cols-2 gap-4">
        {[
          { href: '/tasks/my/published', icon: Briefcase, label: 'My Posted Tasks', sub: 'Tasks you published' },
          { href: '/tasks/my/assigned',  icon: User,      label: 'My Assigned Tasks', sub: 'Tasks you\'re working on' },
        ].map(({ href, icon: Icon, label, sub }) => (
          <a key={href} href={href}
            className="card p-5 text-center hover:shadow-card-hover hover:-translate-y-0.5 transition-all duration-200 cursor-pointer group">
            <div className="w-10 h-10 mx-auto mb-2 rounded-xl bg-gradient-to-br from-brand-100 to-indigo-100 flex items-center justify-center group-hover:from-brand-200 group-hover:to-indigo-200 transition-colors">
              <Icon className="w-5 h-5 text-brand-600" />
            </div>
            <p className="font-bold text-slate-800 text-sm">{label}</p>
            <p className="text-xs text-slate-400 mt-0.5">{sub}</p>
          </a>
        ))}
      </div>
    </div>
  );
};

export default ProfilePage;
